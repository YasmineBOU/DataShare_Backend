package com.openclassrooms.datashare.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating and validating JWT (JSON Web Tokens) for
 * user authentication.
 * This service uses a secret key and expiration time defined in the application
 * properties to create and verify tokens.
 * It interacts with {@link UserDetails} to extract user roles and generate
 * tokens containing this information.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Generating JWT tokens for authenticated users, including their
 * roles.</li>
 * <li>Validating JWT tokens to ensure they are not expired or malformed.</li>
 * <li>Extracting the username from a valid token for further authorization
 * checks.</li>
 * </ul>
 *
 * @see UserDetails
 */
@Service
public class JwtService {

    /**
     * The encoded key used to sign and verify JWT tokens.
     */
    private final Key encodedKey;

    /**
     * The expiration time of JWT tokens in milliseconds.
     */
    private final long jwtExpirationMs;

    /**
     * Constructs a new JwtService with the provided secret key and expiration time.
     *
     * @param key             The secret key used to sign JWT tokens. It can be
     *                        base64-encoded or plain text.
     * @param jwtExpirationMs The expiration time of JWT tokens in milliseconds.
     * @throws IllegalStateException If the provided key is null or empty.
     */
    public JwtService(
            @Value("${com.openclassrooms.datashare.jwt.secret-key}") String key,
            @Value("${com.openclassrooms.datashare.jwt.expiration-ms}") long jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret key must be provided in .env");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(key.trim());
        } catch (IllegalArgumentException ex) {
            keyBytes = key.trim().getBytes(StandardCharsets.UTF_8);
        }

        this.encodedKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token for the given user details, including their roles.
     *
     * @param userDetails The user details containing the username and roles.
     * @return A JWT token as a String, containing the username, roles, issue date,
     *         and expiration date.
     * @throws IllegalArgumentException If the userDetails or its authorities are
     *                                  null.
     */
    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(encodedKey)
                .compact();
    }

    // Validate token and return username if valid, else throw exception
    /**
     * Validates a JWT token and extracts the username if the token is valid.
     *
     * @param token The JWT token to validate.
     * @return The username (subject) extracted from the token if it is valid.
     * @throws RuntimeException         If the token is invalid, expired, or
     *                                  malformed.
     * @throws IllegalArgumentException If the token is null or empty.
     */
    public String validateTokenAndGetUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(encodedKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();

        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid, expired, or malformed
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

}
