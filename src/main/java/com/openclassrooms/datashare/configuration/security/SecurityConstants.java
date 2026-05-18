package com.openclassrooms.datashare.configuration.security;

import java.util.Set;

public final class SecurityConstants {
    private SecurityConstants() {
        // Utility class
    }

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/register",
            "/api/login",
            "/api/logout",
            "/api/files/upload",
            "/api/files/download",
            "/api/files/info"
    };

    public static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/register",
            "/api/login",
            "/api/logout",
            "/api/files/upload",
            "/api/files/download",
            "/api/files/info");

    public static final String AUTH_TOKEN_COOKIE_NAME = "authToken";
    public static final long AUTH_COOKIE_MAX_AGE_SECONDS = 3600L;
}
