package com.openclassrooms.datashare.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing metadata for a file.
 * This class includes information such as filename, size, token, and expiration
 * date.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDTO {
    /**
     * Unique identifier of the file.
     */
    private Long id;

    /**
     * Name of the file.
     */
    private String filename;

    /**
     * Unique token for accessing the file.
     */
    private String fileToken;

    /**
     * Size of the file in bytes.
     */
    private long fileSize;

    /**
     * Timestamp indicating when the file was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp indicating when the file will expire.
     */
    private LocalDateTime expirationDate;

    /**
     * Indicates whether the file is password-protected.
     */
    private Boolean hasPassword;
}
