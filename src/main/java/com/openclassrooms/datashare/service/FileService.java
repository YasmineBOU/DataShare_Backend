package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.dto.FileInfoDTO;
import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.FileRepository;
import com.openclassrooms.datashare.utils.FileUtils;
import com.openclassrooms.datashare.handler.exceptions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.Assert;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final BackblazeB2Service backblazeB2Service;
    private final FileRepository fileDataRepository;
    private final PasswordEncoder passwordEncoder;

    public String uploadFile(MultipartFile uploadedFile, FileData fileData, int expirationDays) {

        Assert.notNull(uploadedFile, "Uploaded file must not be null");
        String fileHash = null;
        try {
            fileHash = FileUtils.calculateFileHash(uploadedFile);
        } catch (IOException e) {
            log.error("Failed to calculate file hash for file: {}", uploadedFile.getOriginalFilename(), e);
            throw new FileHashComputationException("Failed to calculate file hash: " + e.getMessage(), e);
        }

        // Check if received file hash matches the new calculated hash of the received
        // file
        // Prevent potential file corruption during the upload process
        if (!fileHash.equals(fileData.getHash())) {
            log.error("File hash mismatch: expected '{}', calculated '{}'", fileData.getHash(), fileHash);

            throw new FileHashMismatchException("File hash mismatch");
        }
        String presignedUrl = null;
        String key = null;
        try {
            // Generate a unique key for the file in B2 and upload it
            key = backblazeB2Service.uploadFile(uploadedFile, fileData.getEmail());
            // Generate a presigned URL for the uploaded file with the specified expiration
            // time
            presignedUrl = backblazeB2Service.generatePresignedUrl(key, Duration.ofDays(expirationDays)).toString();
            log.info("Generated presigned URL : {}", presignedUrl);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", key, e);
            throw new FileLinkGenerationException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
        if (presignedUrl == null || presignedUrl.isEmpty()) {
            log.error("Presigned URL is empty for key: {}", key);
            throw new FileLinkGenerationException("Presigned URL is empty");
        }

        // Save the file metadata on the database
        String filePassword = fileData.getFilePassword();
        if (filePassword != null && !filePassword.isEmpty()) {
            fileData.setFilePassword(passwordEncoder.encode(filePassword));
        }
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(expirationDays);
        fileData.setExpirationDate(expirationDate);
        fileData.setFileLink(presignedUrl);
        fileData.setFileKey(key); // will be used to identify the file for download and delete operations
        fileDataRepository.save(fileData);

        return presignedUrl;
    }

    public String downloadFile(String fileKey, String filePassword) throws Exception {
        Assert.notNull(fileKey, "File key must not be null");

        Optional<FileData> fileData = fileDataRepository.findByFileKey(fileKey);
        if (fileData.isEmpty()) {
            log.warn("File with key {} not found", fileKey);
            throw new FileNotFoundException("File not found");
        }
        FileData file = fileData.get();
        if (file.getExpirationDate().isBefore(LocalDateTime.now())) {
            log.warn("File with key {} has expired", fileKey);
            throw new FileExpiredException("File has expired");
        }
        if (filePassword != null && file.getFilePassword() != null && !file.getFilePassword().isEmpty()) {
            if (!passwordEncoder.matches(filePassword, file.getFilePassword())) {
                log.warn("Invalid password for file with key '{}'", fileKey);
                throw new InvalidPasswordException("Invalid password");
            }
        }

        String fileLink = file.getFileLink();
        if (fileLink != null && !fileLink.isEmpty()) {
            return fileLink;
        } else {
            log.error("File link is null for file with key '{}'", fileKey);
            throw new FileLinkNullException("File link is null");
        }
    }

    public Iterable<FileInfoDTO> listFiles(User authenticatedUser, String email) {
        Assert.notNull(authenticatedUser, "Authenticated user must not be null");
        Assert.notNull(email, "Email must not be null");
        log.info("Listing files for email: {}", email);
        log.info("Files found: {}", fileDataRepository.findFilesByEmail(email));
        return fileDataRepository.findFilesByEmail(email);
    }

}
