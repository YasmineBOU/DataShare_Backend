package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data Transfer Object (DTO) for uploading a file.
 * This class encapsulates all the necessary information to upload a file,
 * including user association, file metadata, and security settings.
 */
@Data
public class FileUploadDTO {

    /**
     * Email of the user uploading the file (optional).
     * Used to associate the file with a specific user.
     * Must be a valid email format if provided.
     */
    @Email(message = "Email should be valid")
    private String email;

    /**
     * The file to upload.
     * This field is required and cannot be null.
     */
    @NotNull(message = "File is required")
    private MultipartFile file;

    /**
     * Name of the file.
     * This field is required and cannot be blank.
     */
    @NotBlank(message = "Filename is required")
    private String filename;

    /**
     * Size of the file in bytes.
     * This field must be a positive number.
     */
    @Positive(message = "File size must be a positive number")
    private long fileSize;

    /**
     * Type of the file (e.g., "image/png", "application/pdf").
     * This field is required and cannot be blank.
     */
    @NotBlank(message = "File type is required")
    private String fileType;

    /**
     * Hash of the file content for integrity verification.
     * This field is required and cannot be blank.
     */
    @NotBlank(message = "Hash is required")
    private String hash;

    /**
     * Optional password to protect the file.
     * Must be at least 6 characters and contain at least
     * one letter and one digit, but can be long if provided.
     */
    @Size(min = 6, message = "File password must be at least 6 characters long")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]$", message = "File password must contain at least one letter and one digit")
    private String filePassword;

    /**
     * Number of days until the file expires.
     * This field is required and must be a positive number.
     */
    @NotNull(message = "Expiration days is required")
    @Min(value = 1, message = "Expiration days must be a positive number")
    private Integer expirationDays;
}
