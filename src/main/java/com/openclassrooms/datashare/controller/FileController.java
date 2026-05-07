package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.service.FileService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.datashare.dto.FileUploadDTO;
import com.openclassrooms.datashare.mapper.FileDtoMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileDtoMapper fileDtoMapper;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@Valid @RequestBody FileUploadDTO fileUploadDTO) {
        String fileLink = fileService.uploadFile(fileDtoMapper.toEntity(fileUploadDTO));
        return ResponseEntity.ok(Map.of(
                "message", "File uploaded successfully !",
                "fileLink", fileLink));
    }
}
