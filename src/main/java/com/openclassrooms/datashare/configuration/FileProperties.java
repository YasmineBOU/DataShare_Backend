package com.openclassrooms.datashare.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties class for file-related settings.
 * This class is used to bind and validate the file-related properties from the
 * application's configuration file
 * (e.g., `application.properties` or `application.yml`). The properties are
 * prefixed with {@code file}.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Loading and validating file-related configuration properties.</li>
 * <li>Providing a structured way to access file settings, such as forbidden
 * file extensions.</li>
 * </ul>
 *
 * @see ConfigurationProperties
 */
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /**
     * List of file extensions that are forbidden from being uploaded.
     * Example: {@code [".exe", ".bat", ".sh"]}.
     */
    private List<String> forbiddenExtensions;

    /**
     * Maximum allowed file size for uploads.
     * Example: {@code 1GB}, {@code 10MB}, {@code 500KB}.
     */
    private String maxFileSize;

    /**
     * Returns the list of forbidden file extensions.
     *
     * @return A list of forbidden file extensions.
     */
    public List<String> getForbiddenExtensions() {
        return forbiddenExtensions;
    }

    /**
     * Sets the list of forbidden file extensions.
     *
     * @param forbiddenExtensions The list of forbidden extensions to set.
     */
    public void setForbiddenExtensions(List<String> forbiddenExtensions) {
        this.forbiddenExtensions = forbiddenExtensions;
    }

    /**
     * Returns the maximum allowed file size for uploads.
     *
     * @return The maximum file size as a string (e.g., "1GB", "10MB").
     */
    public String getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Sets the maximum allowed file size for uploads.
     *
     * @param maxFileSize The maximum file size to set (e.g., "1GB", "10MB").
     */
    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

}
