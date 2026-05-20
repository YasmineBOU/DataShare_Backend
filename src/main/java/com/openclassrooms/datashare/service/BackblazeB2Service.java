package com.openclassrooms.datashare.service;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import com.openclassrooms.datashare.configuration.BackblazeB2Properties;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class BackblazeB2Service {

    private static final long MULTIPART_THRESHOLD_BYTES = 8L * 1024 * 1024;
    private static final int MULTIPART_CHUNK_SIZE = 8 * 1024 * 1024;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    private final BackblazeB2Properties properties;

    public BackblazeB2Service(
            S3Client s3Client,
            BackblazeB2Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;

        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                properties.getKeyId(),
                                properties.getApplicationKey())))
                .build();
    }

    public String uploadFile(MultipartFile file, String email)
            throws IOException {

        Assert.notNull(file, "File must not be null");
        Assert.isTrue(file.getSize() > 0, "File must not be empty");
        String location = "uploads/";
        if (email != null && !email.isEmpty()) {
            location += email + "/";
        } else {
            location += "anonymous/";
        }

        String key = generateUniqueKey(file.getOriginalFilename(), location);

        if (file.getSize() > MULTIPART_THRESHOLD_BYTES) {
            return uploadMultipartFile(file, email, key);
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(),
                    file.getSize()));
            return key;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    private String uploadMultipartFile(MultipartFile file, String email, String key)
            throws IOException {

        CreateMultipartUploadResponse createMultipartUploadResponse = null;
        try {
            createMultipartUploadResponse = s3Client.createMultipartUpload(
                    CreateMultipartUploadRequest.builder()
                            .bucket(properties.getBucketName())
                            .key(key)
                            .contentType(file.getContentType())
                            .build());

            String uploadId = createMultipartUploadResponse.uploadId();
            List<CompletedPart> completedParts = new ArrayList<>();

            try (InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[MULTIPART_CHUNK_SIZE];
                int bytesRead;
                int partNumber = 1;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byte[] partBytes = Arrays.copyOf(buffer, bytesRead);
                    UploadPartResponse uploadPartResponse = s3Client.uploadPart(
                            UploadPartRequest.builder()
                                    .bucket(properties.getBucketName())
                                    .key(key)
                                    .uploadId(uploadId)
                                    .partNumber(partNumber)
                                    .contentLength((long) bytesRead)
                                    .build(),
                            RequestBody.fromBytes(partBytes));

                    completedParts.add(CompletedPart.builder()
                            .partNumber(partNumber)
                            .eTag(uploadPartResponse.eTag())
                            .build());
                    partNumber++;
                }
            }

            s3Client.completeMultipartUpload(
                    CompleteMultipartUploadRequest.builder()
                            .bucket(properties.getBucketName())
                            .key(key)
                            .uploadId(uploadId)
                            .multipartUpload(CompletedMultipartUpload.builder()
                                    .parts(completedParts)
                                    .build())
                            .build());
            return key;

        } catch (Exception e) {
            if (createMultipartUploadResponse != null && createMultipartUploadResponse.uploadId() != null) {
                try {
                    s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                            .bucket(properties.getBucketName())
                            .key(key)
                            .uploadId(createMultipartUploadResponse.uploadId())
                            .build());
                } catch (Exception abortException) {
                    log.warn("Failed to abort multipart upload for key: {}", key, abortException);
                }
            }
            throw new RuntimeException("Failed to upload multipart file", e);
        }
    }

    public URL generatePresignedUrl(String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }

    private String generateUniqueKey(String originalFilename, String prefix) {
        return prefix + originalFilename + "_" + UUID.randomUUID();
    }

    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(properties.getBucketName()).key(key).build());
        } catch (Exception e) {
            log.error("Failed to delete file with key: '{}'", key, e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

}
