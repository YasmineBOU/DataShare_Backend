package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.FileProperties;
import com.openclassrooms.datashare.dto.FileInfoDTO;
import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.handler.exceptions.*;
import com.openclassrooms.datashare.repository.*;
import com.openclassrooms.datashare.utils.FileUtils;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.Assert;

/**
 * Service responsible for managing file-related operations such as upload,
 * download, listing, retrieval, and deletion.
 * This service interacts with {@link FileRepository} to persist file metadata,
 * {@link BackblazeB2Service} for cloud storage operations,
 * and {@link PasswordEncoder} for securing file passwords. It also handles file
 * validation, expiration, and access control.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Uploading files to Backblaze B2 storage and saving their metadata in the
 * database.</li>
 * <li>Generating secure download links for files, with optional password
 * protection.</li>
 * <li>Listing files associated with a user's email.</li>
 * <li>Retrieving file metadata using a unique token.</li>
 * <li>Deleting files from both the cloud storage and the database (hard or soft
 * delete).</li>
 * </ul>
 *
 * @see FileRepository
 * @see BackblazeB2Service
 * @see PasswordEncoder
 * @see FileProperties
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FileService {
    /**
     * Service for interacting with Backblaze B2 cloud storage.
     */
    private final BackblazeB2Service backblazeB2Service;

    /**
     * Repository for managing file data persistence.
     */
    private final FileRepository fileDataRepository;

    /**
     * Repository for managing user data.
     */
    private final UserRepository userRepository;

    /**
     * Encoder for hashing and verifying file passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Configuration properties for file handling (e.g., forbidden extensions).
     */
    private final FileProperties fileProperties;

    /**
     * Uploads a file to Backblaze B2 storage and saves its metadata in the
     * database.
     *
     * @param uploadedFile   The file to upload (as a {@link MultipartFile}).
     * @param fileData       The metadata associated with the file (e.g., email,
     *                       file password, hash).
     * @param expirationDays The number of days until the file link expires.
     * @return A unique token for accessing the uploaded file.
     * @throws FileExtensionException      If the file has a forbidden extension.
     * @throws FileHashMismatchException   If the provided file hash does not match
     *                                     the calculated hash.
     * @throws FileLinkGenerationException If the presigned URL for the file cannot
     *                                     be generated.
     * @throws RuntimeException            If an error occurs during file
     *                                     processing.
     */
    public String uploadFile(MultipartFile uploadedFile, FileData fileData, int expirationDays) {

        Assert.notNull(uploadedFile, "Uploaded file must not be null");
        Assert.notNull(fileData, "File data must not be null");
        Assert.isTrue(expirationDays > 0, "Expiration days must be a positive number");

        try {

            if (fileProperties.getForbiddenExtensions() != null) {
                String originalFilename = uploadedFile.getOriginalFilename();
                if (originalFilename != null) {
                    String fileExtension = FileUtils.getFileExtension(originalFilename);
                    if (fileProperties.getForbiddenExtensions().contains(fileExtension.toLowerCase())) {
                        log.warn("Attempt to upload file with forbidden extension: {}", fileExtension);
                        throw new FileExtensionException(
                                "Files with extension '" + fileExtension + "' are not allowed");
                    }
                }
            }
            String fileHash = FileUtils.calculateFileHash(uploadedFile);

            if (!fileHash.equals(fileData.getHash())) {
                log.error("File hash mismatch: expected '{}', calculated '{}'", fileData.getHash(), fileHash);
                throw new FileHashMismatchException("File hash mismatch");
            }

            String email = fileData.getEmail();
            String key = backblazeB2Service.uploadFile(uploadedFile, email);
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
            Optional<User> user = userRepository.findByEmail(email);
            fileData.setExpirationDate(expirationDate);
            fileData.setFileLink(presignedUrl.toString());
            fileData.setFileKey(key);
            fileData.setFileToken(fileToken);
            fileData.setUser(user.orElse(null));
            fileDataRepository.save(fileData);

            return fileToken;

        } catch (IOException e) {
            log.error("Failed to process file upload: {}", uploadedFile.getOriginalFilename(), e);
            throw new RuntimeException("Failed to process file upload: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a download link for a file after validating access permissions.
     *
     * @param id           The unique ID of the file to download.
     * @param filePassword The password provided by the user to access the file (if
     *                     applicable).
     * @return A download link for the file.
     * @throws FileNotFoundException    If the file with the given ID does not
     *                                  exist.
     * @throws FileExpiredException     If the file has expired.
     * @throws InvalidPasswordException If the provided password is incorrect or
     *                                  missing when required.
     * @throws FileLinkNullException    If the file link is null or empty.
     */
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
        // Case: File does not have a password but a password was provided
        if (storedFilePassword == null || storedFilePassword.isEmpty()) {
            if (filePassword != null && !filePassword.isEmpty()) {
                log.warn("File with ID {} does not have a password but a password was provided", id);
                throw new InvalidPasswordException("File does not have a password");
            }
        }
        // Case: File has a password
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

    /**
     * Retrieves a list of files associated with a given email.
     *
     * @param authenticatedUser The user making the request (used for
     *                          authorization).
     * @param email             The email of the user whose files are to be listed.
     * @return An iterable of {@link FileInfoDTO} containing file metadata.
     * @throws IllegalArgumentException If the authenticated user or email is null.
     */
    public Iterable<FileInfoDTO> listFiles(User authenticatedUser, String email) {
        Assert.notNull(authenticatedUser, "Authenticated user must not be null");
        Assert.notNull(email, "Email must not be null");
        return fileDataRepository.findFilesByEmail(email);
    }

    /**
     * Retrieves file metadata by its unique token.
     *
     * @param fileToken The unique token identifying the file.
     * @return A {@link FileInfoDTO} containing the file metadata.
     * @throws FileNotFoundException    If no file is found for the given token.
     * @throws IllegalArgumentException If the file token is null.
     */
    public FileInfoDTO getFileInfoByFileToken(String fileToken) {
        Assert.notNull(fileToken, "File token must not be null");
        Optional<FileInfoDTO> fileInfo = fileDataRepository.findFileInfoByFileToken(fileToken);
        if (fileInfo.isEmpty()) {
            log.warn("No file found for file token: {}", fileToken);
            throw new FileNotFoundException("No file found for file token: " + fileToken);
        }
        return fileInfo.get();
    }

    /**
     * Deletes a file from Backblaze B2 storage and removes its metadata from the
     * database.
     *
     * @param authenticatedUser The user making the request (used for
     *                          authorization).
     * @param id                The unique ID of the file to delete.
     * @throws FileNotFoundException    If the file with the given ID does not
     *                                  exist.
     * @throws FileDeletionException    If an error occurs during file deletion.
     * @throws IllegalArgumentException If the authenticated user or file ID is
     *                                  null, or if the ID is not positive.
     */
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
