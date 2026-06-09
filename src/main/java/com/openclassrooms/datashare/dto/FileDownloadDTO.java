package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for downloading a file.
 * This class encapsulates the file ID and an optional password for
 * access-protected files.
 */
@Data
public class FileDownloadDTO {
    /**
     * Unique identifier of the file to download.
     * This field is required and cannot be null.
     */
    @NotNull
    private Long id;

    /**
     * Optional password to access the file.
     * If the file is password-protected, this field must match the file's password.
     */
    private String filePassword;
}
