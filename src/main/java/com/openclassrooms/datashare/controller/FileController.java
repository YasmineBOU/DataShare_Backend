package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.service.FileService;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.openclassrooms.datashare.dto.FileDownloadDTO;
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
                                fileDtoMapper.toEntity(fileUploadDTO),
                                fileUploadDTO.getExpirationDays());
                return ResponseEntity.ok(Map.of(
                                "message", "File uploaded successfully !",
                                "fileLink", fileLink));
        }

        @GetMapping(value = "/download")
        public ResponseEntity<?> downloadFile(@Valid @RequestBody FileDownloadDTO fileDownloadDTO) throws Exception {
                log.info("\n\n\n********Received file download request for fileKey: '{}'\n\n\n",
                                fileDownloadDTO.getFileKey());

                String fileLink = fileService.downloadFile(
                                fileDownloadDTO.getFileKey(),
                                fileDownloadDTO.getFilePassword());

                return ResponseEntity.ok(Map.of(
                                "message", "File link retrieved successfully !",
                                "fileLink", fileLink));
        }

        @GetMapping(value = "/list")
        public ResponseEntity<?> listFiles(@RequestParam String email) {
                return ResponseEntity.ok(Map.of(
                                "message", "Files retrieved successfully !",
                                "files", fileService.listFiles(email)));
        }
}
