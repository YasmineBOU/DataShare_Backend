package com.openclassrooms.datashare.service;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.Assert;

import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.repository.FileRepository;
import com.openclassrooms.datashare.utils.FileUtils;

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

    public String uploadFile(MultipartFile uploadedFile, FileData fileData, int expirationDays) {

        Assert.notNull(uploadedFile, "Uploaded file must not be null");
        try {
            String fileHash = FileUtils.calculateFileHash(uploadedFile);
            // Check if received file hash matches the new calculated hash of the received
            // file
            // Prevent potential file corruption during the upload process
            if (!fileHash.equals(fileData.getHash())) {
                throw new IllegalArgumentException("File hash mismatch");
            }
            // Generate a unique key for the file in B2 and upload it
            String key = backblazeB2Service.uploadFile(uploadedFile, fileData.getEmail());
            // Generate a presigned URL for the uploaded file with the specified expiration
            // time
            String presignedUrl = backblazeB2Service.generatePresignedUrl(
                    key, Duration.ofDays(expirationDays)).toString();
            log.info("Generated presigned URL : {}", presignedUrl);

            if (presignedUrl.isEmpty()) {
                log.error("Failed to generate presigned URL for key: {}", key);
                return "";
            }

            // Save the file metadata on the database
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(expirationDays);
            fileData.setExpirationDate(expirationDate);
            fileData.setFileLink(presignedUrl);
            fileDataRepository.save(fileData);

            return presignedUrl;
        } catch (IllegalArgumentException | IOException e) {
            log.error("Error occurred while calculating file hash:", e);
            // Handle the exception appropriately
            return "Error occurred while uploading file";
        }
    }

}
