package com.openclassrooms.datashare.dto;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FileUploadDTO {

    @Email(message = "Email should be valid")
    private String email; // Optional: to associate the file with a user

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotBlank(message = "Filename is required")
    private String filename;

    @Positive(message = "File size must be a positive number")
    private long fileSize;

    @NotBlank(message = "File type is required")
    private String fileType;

    @NotBlank(message = "Hash is required")
    private String hash; // Checksum for file integrity verification

    @Size(min = 6, message = "File password must be at least 6 characters long")
    private String filePassword; // Optional: password for file access

    @NotNull(message = "Expiration days is required")
    @Min(value = 1, message = "Expiration days must be a positive number")
    private Integer expirationDays; // Number of days until the file expires
}
