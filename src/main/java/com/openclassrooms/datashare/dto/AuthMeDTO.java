package com.openclassrooms.datashare.dto;

/**
 * Data Transfer Object (DTO) representing the authentication status of a user.
 * This record is used to return whether a user is authenticated and their email
 * (if available).
 *
 * @param authenticated Indicates if the user is authenticated.
 * @param email         The email of the authenticated user (can be null if not
 *                      authenticated).
 */
public record AuthMeDTO(
                boolean authenticated,
                String email) {
}