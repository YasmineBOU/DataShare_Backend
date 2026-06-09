package com.openclassrooms.datashare.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties class for Backblaze B2 storage.
 * This class is used to bind and validate the Backblaze B2-related properties
 * from the application's configuration file
 * (e.g., `application.properties` or `application.yml`). The properties are
 * prefixed with {@code b2}.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Loading and validating Backblaze B2 configuration properties.</li>
 * <li>Providing a structured way to access Backblaze B2 settings.</li>
 * </ul>
 *
 * @see ConfigurationProperties
 */
@Configuration
@ConfigurationProperties(prefix = "b2")
public class BackblazeB2Properties {
    /**
     * Backblaze B2 endpoint URL.
     * Example: {@code https://s3.eu-central-003.backblazeb2.com}.
     */
    private String endpoint;

    /**
     * Backblaze B2 region.
     * Example: {@code eu-central-003}.
     */
    private String region;

    /**
     * Name of the Backblaze B2 bucket where files are stored.
     */
    private String bucketName;

    /**
     * Backblaze B2 key ID.
     * Used for authentication with Backblaze B2.
     */
    private String keyId;

    /**
     * Backblaze B2 application key.
     * Used for authentication with Backblaze B2.
     */
    private String applicationKey;

    /**
     * Returns the Backblaze B2 endpoint URL.
     *
     * @return The endpoint URL.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the Backblaze B2 endpoint URL.
     *
     * @param endpoint The endpoint URL to set.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the Backblaze B2 region.
     *
     * @return The region.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the Backblaze B2 region.
     *
     * @param region The region to set.
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Returns the name of the Backblaze B2 bucket.
     *
     * @return The bucket name.
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the name of the Backblaze B2 bucket.
     *
     * @param bucketName The bucket name to set.
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Returns the Backblaze B2 key ID.
     *
     * @return The key ID.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Sets the Backblaze B2 key ID.
     *
     * @param keyId The key ID to set.
     */
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    /**
     * Returns the Backblaze B2 application key.
     *
     * @return The application key.
     */
    public String getApplicationKey() {
        return applicationKey;
    }

    /**
     * Sets the Backblaze B2 application key.
     *
     * @param applicationKey The application key to set.
     */
    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }
}
