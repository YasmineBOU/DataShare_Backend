package com.openclassrooms.datashare.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class FileUploadDTO {
    private String email; // Optional: to associate the file with a user
    private MultipartFile file;
    private String filename;
    private long fileSize;
    private String fileType;
    private String hash; // Checksum for file integrity verification
    private String filePassword; // Optional: password for file access
    private Integer expirationDays; // Number of days until the file expires
}