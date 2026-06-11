package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.FileDownloadDTO;
import com.openclassrooms.datashare.dto.FileInfoDTO;
import com.openclassrooms.datashare.dto.FileUploadDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.mapper.FileDtoMapper;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.service.FileService;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing file operations (upload, download, list, info,
 * delete).
 * This controller exposes endpoints to interact with the {@link FileService}
 * and maps data using {@link FileDtoMapper}.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

        /**
         * Repository for managing user data.
         */
        private final UserRepository userRepository;

        /**
         * Service for handling business logic related to files.
         */
        private final FileService fileService;
        /**
         * Mapper to convert between DTOs and entities.
         */
        private final FileDtoMapper fileDtoMapper;

        /**
         * Uploads a file to the server.
         *
         * @param fileUploadDTO DTO containing the file to upload, its metadata, and
         *                      expiration duration.
         * @return ResponseEntity with a success message and a token to access the file.
         */
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> uploadFile(@Valid @ModelAttribute FileUploadDTO fileUploadDTO) {
                String fileLink = fileService.uploadFile(
                                fileUploadDTO.getFile(),
                                fileDtoMapper.toEntity(fileUploadDTO, userRepository),
                                fileUploadDTO.getExpirationDays());
                return ResponseEntity.ok(Map.of(
                                "message", "File uploaded successfully !",
                                "fileToken", fileLink));
        }

        /**
         * Retrieves a download link for a file using its ID and a possible password.
         *
         * @param fileDownloadDTO DTO containing the file ID and password to access it.
         * @return ResponseEntity with a success message and a download link.
         * @throws Exception If the file does not exist or the password is incorrect.
         */
        @PostMapping(value = "/download")
        public ResponseEntity<?> downloadFile(@Valid @RequestBody FileDownloadDTO fileDownloadDTO) throws Exception {

                String fileLink = fileService.downloadFile(
                                fileDownloadDTO.getId(),
                                fileDownloadDTO.getFilePassword());

                return ResponseEntity.ok(Map.of(
                                "message", "File link retrieved successfully !",
                                "fileLink", fileLink));
        }

        /**
         * Lists files accessible by a given user.
         *
         * @param authenticatedUser Authenticated user.
         * @param email             Email of the user whose files are to be listed.
         * @return ResponseEntity with a success message and the list of files.
         */
        @GetMapping(value = "/list")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> listFiles(
                        @AuthenticationPrincipal User authenticatedUser,
                        @RequestParam String email) {
                return ResponseEntity.ok(Map.of(
                                "message", "Files retrieved successfully !",
                                "files", fileService.listFiles(authenticatedUser, email)));
        }

        /**
         * Retrieves metadata for a file using its token.
         *
         * @param authenticatedUser Authenticated user.
         * @param fileToken         Unique token identifying the file.
         * @return ResponseEntity containing the file metadata as a {@link FileInfoDTO}.
         * @throws Exception If the file does not exist or the user lacks access rights.
         */
        @GetMapping(value = "/info")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> getFileInfo(
                        @AuthenticationPrincipal User authenticatedUser,
                        @RequestParam String fileToken) throws Exception {

                FileInfoDTO fileInfo = fileService.getFileInfoByFileToken(fileToken);
                return ResponseEntity.ok(fileInfo);
        }

        /**
         * Deletes a file from the server.
         *
         * @param authenticatedUser Authenticated user
         * @param id                Unique ID of the file to delete.
         * @return ResponseEntity with a success message.
         */
        @DeleteMapping(value = "/delete/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> deleteFile(
                        @AuthenticationPrincipal User authenticatedUser,
                        @PathVariable Long id) {
                fileService.deleteFile(authenticatedUser, id);
                return ResponseEntity.ok(Map.of(
                                "message", "File deleted successfully !"));
        }
}
