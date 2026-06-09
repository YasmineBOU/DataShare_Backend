package com.openclassrooms.datashare.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuration class for setting up the Backblaze B2 S3-compatible client.
 * This class loads the necessary credentials and settings from the
 * application's properties
 * (typically from a `.env` file) and creates an {@link S3Client} bean
 * configured to interact
 * with Backblaze B2 storage.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Loading Backblaze B2 endpoint, credentials, and region from
 * configuration.</li>
 * <li>Creating and configuring an {@link S3Client} bean for Backblaze B2.</li>
 * <li>Validating the presence of required configuration values.</li>
 * </ul>
 *
 * @see S3Client
 * @see AwsBasicCredentials
 * @see StaticCredentialsProvider
 */
@Configuration
@Slf4j
public class BackblazeB2Config {

    /**
     * Backblaze B2 endpoint URL.
     * Example: {@code https://s3.eu-central-003.backblazeb2.com}.
     */
    @Value("${b2.endpoint}")
    private String endpoint;

    /**
     * Backblaze B2 key ID.
     * Used for authentication with Backblaze B2.
     */
    @Value("${b2.key-id}")
    private String keyId;

    /**
     * Backblaze B2 application key.
     * Used for authentication with Backblaze B2.
     */
    @Value("${b2.application-key}")
    private String applicationKey;

    /**
     * Backblaze B2 region.
     * Example: {@code eu-central-003}.
     */
    @Value("${b2.region}")
    private String region;

    /**
     * Creates and configures an {@link S3Client} bean for Backblaze B2.
     *
     * <p>
     * The client is configured with:
     * <ul>
     * <li>Endpoint override to point to Backblaze B2.</li>
     * <li>Region for the Backblaze B2 bucket.</li>
     * <li>Static credentials (key ID and application key) for authentication.</li>
     * </ul>
     *
     * @return A configured {@link S3Client} instance for Backblaze B2.
     * @throws IllegalStateException If the Backblaze B2 endpoint is not provided or
     *                               is blank.
     */
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
