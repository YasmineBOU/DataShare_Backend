package com.openclassrooms.datashare.dto;

import lombok.Data;

@Data
public class FileDownloadDTO {
    private String fileKey;
    private String filePassword; // Optional password for files that require one
}
