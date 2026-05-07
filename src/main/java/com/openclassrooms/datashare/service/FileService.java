package com.openclassrooms.datashare.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.Assert;

import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.utils.FileUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FileService {

    public String uploadFile(MultipartFile uploadedFile, FileData fileData) {
        log.info("\n\n\n********Received file data: {}\n\n\n", fileData);
        Assert.notNull(uploadedFile, "Uploaded file must not be null");
        try {
            String fileHash = FileUtils.calculateFileHash(uploadedFile, "SHA-256");
            log.info("\n\n\n********Calculated file hash: {}\n\n\n", fileHash);
            return "File uploaded successfully";
        } catch (IOException e) {
            log.error("Error occurred while calculating file hash:", e);
            // Handle the exception appropriately
            return "Error occurred while uploading file";
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm not found:", e);
            // Handle the exception appropriately
            return "Error occurred while uploading file";
        }
    }

}
