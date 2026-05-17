package com.openclassrooms.datashare.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDTO {
    private Long id;
    private String filename;
    private long fileSize;
    private LocalDateTime createdAt;
    private LocalDateTime expirationDate;
    private Boolean hasPassword;
}
