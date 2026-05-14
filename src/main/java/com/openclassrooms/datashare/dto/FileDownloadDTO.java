package com.openclassrooms.datashare.dto;

import lombok.Data;

@Data
public class FileDownloadDTO {
    private Long id;
    private String filePassword; // Optional password for files that require one
}
