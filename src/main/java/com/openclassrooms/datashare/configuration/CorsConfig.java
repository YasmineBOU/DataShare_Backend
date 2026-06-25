package com.openclassrooms.datashare.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS) settings.
 * This class defines a {@link CorsConfigurationSource} bean to configure CORS
 * policies
 * for the application, allowing secure communication between the frontend and
 * backend.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Enabling CORS for specific origins (e.g., frontend URLs).</li>
 * <li>Allowing credentials (cookies, authorization headers).</li>
 * <li>Configuring allowed HTTP methods and headers.</li>
 * <li>Setting a max age for preflight requests to optimize performance.</li>
 * </ul>
 *
 * @see CorsConfiguration
 * @see CorsConfigurationSource
 */
@Configuration
public class CorsConfig {

    /**
     * Creates and configures a {@link CorsConfigurationSource} bean to define CORS
     * policies.
     *
     * <p>
     * The CORS configuration allows:
     * <ul>
     * <li>Credentials (cookies, authorization headers) to be included in
     * requests.</li>
     * <li>Requests from the following origins:
     * <ul>
     * <li>Frontend running on port 4200 (HTTP and HTTPS)</li>
     * <li>Frontend running on port 3000 (HTTP and HTTPS)</li>
     * </ul>
     * </li>
     * <li>HTTP methods: GET, POST, PUT, DELETE, OPTIONS, PATCH.</li>
     * <li>All headers to be included in requests.</li>
     * <li>Exposure of custom headers like "Authorization" and "Content-Type".</li>
     * <li>Caching of preflight requests for 3600 seconds (1 hour).</li>
     * </ul>
     *
     * @return A configured {@link CorsConfigurationSource} instance.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Frontend URL - adjust to your environment
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "https://localhost:4200",
                "http://localhost",
                "https://localhost",
                "http://localhost:8081",
                "https://localhost:8081"));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Expose headers if needed (e.g., for pagination, custom headers)
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Max age for preflight requests (in seconds)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
