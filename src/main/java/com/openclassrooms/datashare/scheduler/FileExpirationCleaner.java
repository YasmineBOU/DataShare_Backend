package com.openclassrooms.datashare.scheduler;

import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.repository.FileRepository;
import com.openclassrooms.datashare.service.BackblazeB2Service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled component responsible for cleaning up expired files.
 * This component runs periodically based on a cron expression defined in the
 * application properties.
 * It identifies files that have exceeded their expiration date, deletes them
 * from Backblaze B2 storage,
 * and marks them as deleted in the database.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Scheduling automatic cleanup of expired files.</li>
 * <li>Deleting files from Backblaze B2 storage.</li>
 * <li>Marking expired files as deleted in the database.</li>
 * </ul>
 *
 * @see FileRepository
 * @see BackblazeB2Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileExpirationCleaner {

    /**
     * Repository for managing file data persistence.
     */
    private final FileRepository fileRepository;

    /**
     * Service for interacting with Backblaze B2 cloud storage.
     */
    private final BackblazeB2Service backblazeB2Service;

    /**
     * Scheduled task that runs periodically to clean up expired files.
     * This method:
     * <ol>
     * <li>Retrieves the current date and time.</li>
     * <li>Finds all files that have expired (based on their expiration date).</li>
     * <li>Deletes each expired file from Backblaze B2 storage.</li>
     * <li>Marks the file as deleted in the database.</li>
     * <li>Logs the cleanup process and any errors encountered.</li>
     * </ol>
     *
     * @throws RuntimeException If an error occurs during the deletion of a file
     *                          from Backblaze B2.
     *                          Errors are logged and do not interrupt the cleanup
     *                          process for other files.
     */
    @Scheduled(cron = "${scheduler.file-expiration.cron}")
    public void cleanExpiredFiles() {
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
