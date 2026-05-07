package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.service.FileService;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.openclassrooms.datashare.dto.FileUploadDTO;
import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.mapper.FileDtoMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;
    private final FileDtoMapper fileDtoMapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@Valid @ModelAttribute FileUploadDTO fileUploadDTO) {
        log.info("\n\n\n********Received file upload request: {}\n\n\n",
                fileUploadDTO);
        String fileLink = fileService.uploadFile(
                fileUploadDTO.getFile(),
                fileDtoMapper.toEntity(fileUploadDTO));
        return ResponseEntity.ok(Map.of(
                "message", "File uploaded successfully !",
                "fileLink", fileLink));
    }

}
