package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for user registration requests.
 * This class encapsulates the user's email and password for registration
 * operations.
 *
 * <p>
 * Validation rules:
 * <ul>
 * <li>The email must be valid and not blank.</li>
 * <li>The password must be at least 8 characters long.</li>
 * <li>The password must contain at least one digit, one lowercase letter, and
 * one uppercase letter.</li>
 * </ul>
 */
@Data
public class RegisterDTO {
    /**
     * User's email address.
     * Must be a valid email format and not blank.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * User's password.
     * Must meet the following criteria:
     * <ul>
     * <li>At least 8 characters long.</li>
     * <li>Contains at least one digit (0-9).</li>
     * <li>Contains at least one lowercase letter (a-z).</li>
     * <li>Contains at least one uppercase letter (A-Z).</li>
     * </ul>
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", message = "Password must contain at least one digit, one lowercase letter, and one uppercase letter")
    private String password;
}
