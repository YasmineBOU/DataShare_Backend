package com.openclassrooms.datashare.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Frontend URL - adjust to your environment
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "https://localhost:4200",
                "http://localhost:3000",
                "https://localhost:3000"));

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
