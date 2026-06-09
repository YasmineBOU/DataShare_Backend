package com.openclassrooms.datashare.configuration.security;

import java.util.Set;

/**
 * Utility class containing security-related constants for the DataShare
 * application.
 * This class defines public endpoints, authentication token configurations, and
 * other security settings.
 * It is designed as a final utility class with a private constructor to prevent
 * instantiation.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Defining public endpoints that do not require authentication.</li>
 * <li>Configuring authentication token settings (e.g., cookie name and max
 * age).</li>
 * </ul>
 */
public final class SecurityConstants {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SecurityConstants() {
        // Utility class
    }

    /**
     * Array of public endpoints that do not require authentication.
     * These endpoints are accessible to all users, including unauthenticated ones.
     *
     * <p>
     * Endpoints include:
     * <ul>
     * <li>{@code /api/register} - User registration endpoint.</li>
     * <li>{@code /api/login} - User login endpoint.</li>
     * <li>{@code /api/logout} - User logout endpoint.</li>
     * <li>{@code /api/files/upload} - File upload endpoint.</li>
     * <li>{@code /api/files/download} - File download endpoint.</li>
     * <li>{@code /api/files/info} - File metadata retrieval endpoint.</li>
     * </ul>
     */
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/register",
            "/api/login",
            "/api/logout",
            "/api/files/upload",
            "/api/files/download",
            "/api/files/info"
    };

    /**
     * Set of public endpoints that do not require authentication.
     * This set is used for efficient lookups when checking if a path is public.
     *
     * <p>
     * Endpoints include:
     * <ul>
     * <li>{@code /api/register}</li>
     * <li>{@code /api/login}</li>
     * <li>{@code /api/logout}</li>
     * <li>{@code /api/files/upload}</li>
     * <li>{@code /api/files/download}</li>
     * <li>{@code /api/files/info}</li>
     * </ul>
     */
    public static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/register",
            "/api/login",
            "/api/logout",
            "/api/files/upload",
            "/api/files/download",
            "/api/files/info");

    /**
     * Name of the cookie used to store the authentication token.
     * This cookie is set upon successful login and used for subsequent
     * authenticated requests.
     */
    public static final String AUTH_TOKEN_COOKIE_NAME = "authToken";

    /**
     * Maximum age of the authentication token cookie in seconds.
     * After this period, the cookie expires and the user must log in again.
     */
    public static final long AUTH_COOKIE_MAX_AGE_SECONDS = 3600L; // 1 hour
}
