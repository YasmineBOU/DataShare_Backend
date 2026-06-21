package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for user authentication requests.
 * This class encapsulates the user's email and password for login operations.
 *
 */
@Data
public class AuthDTO {
    /**
     * User's email address.
     * Must be a valid email format and not blank.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * User's password.
     */
    @NotBlank(message = "Password is required")
    private String password;
}
