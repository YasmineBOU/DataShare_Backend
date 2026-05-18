package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.dto.FileInfoDTO;
import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.FileRepository;
import com.openclassrooms.datashare.utils.FileUtils;
import com.openclassrooms.datashare.handler.exceptions.*;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

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
        Assert.notNull(fileData, "File data must not be null");
        Assert.isTrue(expirationDays > 0, "Expiration days must be a positive number");

        try {
            String fileHash = FileUtils.calculateFileHash(uploadedFile);

            if (!fileHash.equals(fileData.getHash())) {
                log.error("File hash mismatch: expected '{}', calculated '{}'", fileData.getHash(), fileHash);
                throw new FileHashMismatchException("File hash mismatch");
            }

            String key = backblazeB2Service.uploadFile(uploadedFile, fileData.getEmail());
            URL presignedUrl = backblazeB2Service.generatePresignedUrl(key, Duration.ofDays(expirationDays));

            if (presignedUrl == null || presignedUrl.toString().isEmpty()) {
                log.error("Presigned URL is empty for key: {}", key);
                throw new FileLinkGenerationException("Presigned URL is empty");
            }

            String filePassword = fileData.getFilePassword();
            if (filePassword != null && !filePassword.isEmpty()) {
                fileData.setFilePassword(passwordEncoder.encode(filePassword));
            }
            String fileToken = FileUtils.generateUniqueFileToken(fileHash, key, 20);
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(expirationDays);
            fileData.setExpirationDate(expirationDate);
            fileData.setFileLink(presignedUrl.toString());
            fileData.setFileKey(key);
            fileData.setFileToken(fileToken);
            fileDataRepository.save(fileData);

            return fileToken;

        } catch (IOException e) {
            log.error("Failed to process file upload: {}", uploadedFile.getOriginalFilename(), e);
            throw new RuntimeException("Failed to process file upload: " + e.getMessage(), e);
        }
    }

    public String downloadFile(Long id, String filePassword) {
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

    public FileInfoDTO getFileInfoByFileToken(String fileToken) {
        Assert.notNull(fileToken, "File token must not be null");
        Optional<FileInfoDTO> fileInfo = fileDataRepository.findFileInfoByFileToken(fileToken);
        if (fileInfo.isEmpty()) {
            log.warn("No file found for file token: {}", fileToken);
            throw new FileNotFoundException("No file found for file token: " + fileToken);
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
            throw new FileNotFoundException("File with id " + id + " not found in database");
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
