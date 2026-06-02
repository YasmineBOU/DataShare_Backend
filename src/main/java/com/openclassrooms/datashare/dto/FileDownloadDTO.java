package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileDownloadDTO {
    @NotNull
    private Long id;
    private String filePassword; // Optional password for files that require one
}
