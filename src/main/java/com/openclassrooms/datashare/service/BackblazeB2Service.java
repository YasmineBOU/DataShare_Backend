package com.openclassrooms.datashare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.openclassrooms.datashare.configuration.BackblazeB2Properties;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
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
        String location = "uploads/";
        if (email != null && !email.isEmpty()) {
            location += email + "/";
        } else {
            location += "anonymous/";
        }

        String key = generateUniqueKey(file.getOriginalFilename(), location);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            return key;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier", e);
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
