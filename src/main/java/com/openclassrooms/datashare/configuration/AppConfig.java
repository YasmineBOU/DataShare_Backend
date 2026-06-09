package com.openclassrooms.datashare.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

/**
 * Configuration class for managing application properties and environment
 * variables.
 * This class defines a {@link PropertySourcesPlaceholderConfigurer} bean to
 * load properties
 * from an external `.env` file, enabling dynamic configuration of the
 * application.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Loading properties from an external `.env` file.</li>
 * <li>Enabling the use of placeholders in Spring configuration files.</li>
 * </ul>
 *
 * @see PropertySourcesPlaceholderConfigurer
 */
@Configuration
public class AppConfig {

    /**
     * Creates and configures a {@link PropertySourcesPlaceholderConfigurer} bean to
     * load properties
     * from an external `.env` file located in the root directory of the
     * application.
     *
     * <p>
     * The `.env` file is used to store environment-specific properties such as:
     * <ul>
     * <li>Database credentials</li>
     * <li>JWT secret keys</li>
     * <li>Backblaze B2 storage configurations</li>
     * <li>Other sensitive or environment-specific settings</li>
     * </ul>
     *
     * @return A configured {@link PropertySourcesPlaceholderConfigurer} instance.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocation(new FileSystemResource(".env"));
        return configurer;
    }
}
