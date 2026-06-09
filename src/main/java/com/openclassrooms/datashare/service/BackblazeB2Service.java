package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.BackblazeB2Properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

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

/**
 * Service responsible for interacting with Backblaze B2 cloud storage using the
 * AWS SDK for Java (S3-compatible API).
 * This service handles file uploads (including multipart uploads), presigned
 * URL generation, and file deletion.
 * It uses configurations from {@link BackblazeB2Properties} to connect to the
 * Backblaze B2 storage.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Uploading files to Backblaze B2 storage, with support for multipart
 * uploads for large files.</li>
 * <li>Generating presigned URLs for secure file access.</li>
 * <li>Deleting files from Backblaze B2 storage.</li>
 * <li>Generating unique keys for files to ensure no naming conflicts.</li>
 * </ul>
 *
 * @see BackblazeB2Properties
 * @see S3Client
 * @see S3Presigner
 */
@Service
@Slf4j
public class BackblazeB2Service {

    /**
     * Threshold size (in bytes) above which files are uploaded using multipart
     * upload.
     */
    private static final long MULTIPART_THRESHOLD_BYTES = 8L * 1024 * 1024;

    /**
     * Size of each chunk (in bytes) for multipart uploads.
     */
    private static final int MULTIPART_CHUNK_SIZE = 8 * 1024 * 1024;

    /**
     * AWS S3 client used to interact with Backblaze B2 storage.
     */
    private final S3Client s3Client;

    /**
     * AWS S3 presigner used to generate presigned URLs for file access.
     */
    private final S3Presigner s3Presigner;

    /**
     * Configuration properties for Backblaze B2 storage.
     */
    private final BackblazeB2Properties properties;

    /**
     * Constructs a new BackblazeB2Service with the provided S3 client, presigner,
     * and properties.
     *
     * @param s3Client   The AWS S3 client configured for Backblaze B2.
     * @param properties The configuration properties for Backblaze B2 storage.
     */
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

    /**
     * Uploads a file to Backblaze B2 storage.
     *
     * @param file  The file to upload (as a {@link MultipartFile}).
     * @param email The email of the user uploading the file (optional, used for
     *              organizing files).
     * @return A unique key identifying the uploaded file in Backblaze B2 storage.
     * @throws IllegalArgumentException If the file is null or empty.
     * @throws RuntimeException         If the file upload fails.
     * @throws IOException              If the file cannot be read.
     */
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

    /**
     * Uploads a large file to Backblaze B2 storage using multipart upload.
     *
     * @param file  The file to upload (as a {@link MultipartFile}).
     * @param email The email of the user uploading the file (optional, used for
     *              organizing files).
     * @param key   The unique key for the file in Backblaze B2 storage.
     * @return The unique key identifying the uploaded file.
     * @throws IOException      If the file cannot be read or uploaded.
     * @throws RuntimeException If the multipart upload fails.
     */
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

    /**
     * Generates a presigned URL for accessing a file in Backblaze B2 storage.
     *
     * @param key      The unique key identifying the file in Backblaze B2 storage.
     * @param duration The duration for which the presigned URL is valid.
     * @return A presigned URL as a {@link URL} object.
     */
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

    /**
     * Generates a unique key for a file to avoid naming conflicts in Backblaze B2
     * storage.
     *
     * @param originalFilename The original name of the file.
     * @param prefix           The prefix to prepend to the filename (e.g.,
     *                         "uploads/user@example.com/").
     * @return A unique key combining the prefix, original filename, and a random
     *         UUID.
     */
    private String generateUniqueKey(String originalFilename, String prefix) {
        return prefix + originalFilename + "_" + UUID.randomUUID();
    }

    /**
     * Deletes a file from Backblaze B2 storage.
     *
     * @param key The unique key identifying the file to delete.
     * @throws RuntimeException If the file deletion fails.
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(properties.getBucketName()).key(key).build());
        } catch (Exception e) {
            log.error("Failed to delete file with key: '{}'", key, e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

}
