package com.openclassrooms.datashare.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileInfoDTO {
    private Long id;
    private String fileName;
    private LocalDateTime expirationDate;
    private Boolean hasPassword;
}
