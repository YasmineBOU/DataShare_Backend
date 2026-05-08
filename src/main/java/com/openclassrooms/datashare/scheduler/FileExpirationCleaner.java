package com.openclassrooms.datashare.scheduler;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.repository.FileRepository;
import com.openclassrooms.datashare.service.BackblazeB2Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.event.ContextRefreshedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileExpirationCleaner {

    private final FileRepository fileRepository;
    private final BackblazeB2Service backblazeB2Service;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationStarted() {
        LocalDateTime currentDate = LocalDateTime.now();
        List<FileData> expiredFiles = fileRepository.findExpiredFiles(currentDate);

        for (FileData file : expiredFiles) {
            try {
                // Delete the file from Backblaze B2 and
                backblazeB2Service.deleteFile(file.getFileKey());
                // Set the 'is_deleted' to true for the record from on database
                file.setDeleted(true);
                fileRepository.save(file);
                log.info("Expired file deleted : " + file.getFileKey());
            } catch (Exception e) {
                log.warn("Got error while deleting file " + file.getFileKey() + " : " + e.getMessage());
            }
        }

        log.info("File expiration cleanup completed. " + expiredFiles.size() + " files deleted.");
    }
}
