package com.openclassrooms.datashare.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Slf4j
public class BackblazeB2Config {

    @Value("${b2.endpoint}")
    private String endpoint;

    @Value("${b2.key-id}")
    private String keyId;

    @Value("${b2.application-key}")
    private String applicationKey;

    @Value("${b2.region}")
    private String region;

    @Bean
    public S3Client s3Client() {

        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("B2 endpoint must be provided in .env.");
        }
        return S3Client.builder()
                .endpointOverride(java.net.URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(keyId, applicationKey)))
                .build();
    }
}
