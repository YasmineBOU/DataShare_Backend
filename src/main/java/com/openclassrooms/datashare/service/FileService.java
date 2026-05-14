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
import java.util.Optional;

import org.apache.tomcat.jni.FileInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.Assert;

import jakarta.transaction.Transactional;
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

    public String downloadFile(Long id, String filePassword) throws Exception {
        Assert.notNull(id, "File ID must not be null");

        Optional<FileData> fileData = fileDataRepository.findById(id);
        if (fileData.isEmpty()) {
            log.warn("File with ID {} not found", id);
            throw new FileNotFoundException("File not found");
        }
        FileData file = fileData.get();
        if (file.getExpirationDate().isBefore(LocalDateTime.now())) {
            log.warn("File with ID {} has expired", id);
            throw new FileExpiredException("File has expired");
        }

        String storedFilePassword = file.getFilePassword();
        // Password provided but file does not have a passwordstoredFilePassword));
        if (storedFilePassword == null || storedFilePassword.isEmpty()) {
            if (filePassword != null && !filePassword.isEmpty()) {
                log.warn("File with ID {} does not have a password but a password was provided", id);
                throw new InvalidPasswordException("File does not have a password");
            }
        }
        // File has a password
        else {
            // No password provided by the user
            if (filePassword == null || filePassword.isEmpty()) {
                log.warn("File with ID {} has a password but no password was provided", id);
                throw new InvalidPasswordException("Password is required to access this file");
            }
            // Password provided but does not match the stored password
            else if (!passwordEncoder.matches(filePassword, storedFilePassword)) {
                log.warn("Invalid password provided for file with ID '{}'", id);
                throw new InvalidPasswordException("Invalid password");
            }
        }

        String fileLink = file.getFileLink();
        if (fileLink != null && !fileLink.isEmpty()) {
            return fileLink;
        } else {
            log.error("File link is null for file with ID '{}'", id);
            throw new FileLinkNullException("File link is null");
        }
    }

    public Iterable<FileInfoDTO> listFiles(User authenticatedUser, String email) {
        Assert.notNull(authenticatedUser, "Authenticated user must not be null");
        Assert.notNull(email, "Email must not be null");
        return fileDataRepository.findFilesByEmail(email);
    }

    public FileInfoDTO getFileInfo(Long fileId) throws Exception {
        Assert.notNull(fileId, "File ID must not be null");
        Optional<FileInfoDTO> fileInfo = fileDataRepository.findFileInfoById(fileId);
        if (fileInfo.isEmpty()) {
            log.warn("No file found for file ID: {}", fileId);
            throw new FileNotFoundException("No file found for file ID: " + fileId);
        }
        return fileInfo.get();
    }

    public void deleteFile(User authenticatedUser, Long id) {
        Assert.notNull(authenticatedUser, "Authenticated user must not be null");
        Assert.notNull(id, "File ID must not be null");
        Assert.isTrue(id > 0, "File ID must be a positive number");

        Optional<FileData> fileData = fileDataRepository.findById(id);
        if (fileData.isEmpty()) {
            log.warn("File with id {} not found for deletion", id);
            throw new IllegalStateException("File with id " + id + " not found in database");
        }
        FileData file = fileData.get();
        try {
            backblazeB2Service.deleteFile(file.getFileKey());
            // Hard delete: remove the file record from the database
            fileDataRepository.delete(file);
            // Soft delete: mark the file as deleted in the database without actually
            // removing the record (uncomment if you want to proceed this way)
            /*
             * file.setDeleted(true);
             * fileDataRepository.save(file);
             */
        } catch (Exception e) {
            log.error("Failed to delete file with id {}: {}", id, e.getMessage(), e);
            throw new FileDeletionException("Failed to delete file: " + e.getMessage(), e);
        }
    }

}
