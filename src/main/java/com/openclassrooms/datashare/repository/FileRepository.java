package com.openclassrooms.datashare.repository;

import com.openclassrooms.datashare.dto.FileInfoDTO;
import com.openclassrooms.datashare.entities.FileData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link FileData} entities in the database.
 * This interface provides CRUD operations and custom queries for file-related
 * data,
 * including finding files by email, file key, expiration date, and file token.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Finding files by email.</li>
 * <li>Finding files by file key.</li>
 * <li>Retrieving file metadata by email.</li>
 * <li>Finding expired files based on their expiration date.</li>
 * <li>Retrieving file metadata by file token.</li>
 * </ul>
 *
 * @see FileData
 * @see FileInfoDTO
 * @see JpaRepository
 */
@Repository
public interface FileRepository extends JpaRepository<FileData, Long> {
        /**
         * Finds a file by its unique file key.
         *
         * @param fileKey The unique file key.
         * @return An {@link Optional} containing the {@link FileData} entity if found,
         *         or empty otherwise.
         */
        Optional<FileData> findByFileKey(String fileKey);

        /**
         * Retrieves file metadata for all files associated with a given email.
         * The metadata is returned as a list of {@link FileInfoDTO} objects, including:
         * file ID, filename, file token, file size, creation date, expiration date, and
         * whether the file is password-protected.
         *
         * @param email The email of the user whose files are to be retrieved.
         * @return A list of {@link FileInfoDTO} objects representing the files.
         */
        @Query("SELECT new com.openclassrooms.datashare.dto.FileInfoDTO(" +
                        "f.id, " +
                        "f.filename, " +
                        "f.fileToken, " +
                        "f.fileSize, " +
                        "f.createdAt, " +
                        "f.expirationDate, " +
                        "CASE WHEN f.filePassword IS NOT NULL AND f.filePassword != '' THEN true ELSE false END) " +
                        "FROM FileData f WHERE f.user.email = :email")
        List<FileInfoDTO> findFilesByEmail(String email);

        /**
         * Finds all files that have expired based on their expiration date.
         *
         * @param currentDate The current date and time to compare against file
         *                    expiration dates.
         * @return A list of {@link FileData} entities that have expired.
         */
        @Query("SELECT f FROM FileData f WHERE f.expirationDate < :currentDate")
        List<FileData> findExpiredFiles(@Param("currentDate") LocalDateTime currentDate);

        /**
         * Retrieves file metadata for a file identified by its unique token.
         * The metadata is returned as a {@link FileInfoDTO} object, including:
         * file ID, filename, file token, file size, creation date, expiration date, and
         * whether the file is password-protected.
         *
         * @param fileToken The unique token identifying the file.
         * @return An {@link Optional} containing the {@link FileInfoDTO} if found, or
         *         empty otherwise.
         */
        @Query("SELECT new com.openclassrooms.datashare.dto.FileInfoDTO(" +
                        "f.id, " +
                        "f.filename, " +
                        "f.fileToken, " +
                        "f.fileSize, " +
                        "f.createdAt, " +
                        "f.expirationDate, " +
                        "CASE WHEN f.filePassword IS NOT NULL AND f.filePassword != '' THEN true ELSE false END) " +
                        "FROM FileData f WHERE f.fileToken = :fileToken")
        Optional<FileInfoDTO> findFileInfoByFileToken(@Param("fileToken") String fileToken);

}
