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

@Service
public class JwtService {

    private final Key encodedKey;
    private final long jwtExpirationMs;

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
     * Generate a JWT token containing the user's username and roles.
     * 
     * @param userDetails the user details containing the username and roles
     * @return a JWT token containing the username and roles
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
