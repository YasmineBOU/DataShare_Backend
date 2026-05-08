package com.openclassrooms.datashare.repository;

import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.dto.FileInfoDTO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileData, Long> {
    Optional<FileData> findByEmail(String email);

    Optional<FileData> findByFileKey(String fileKey);

    @Query("SELECT new com.openclassrooms.datashare.dto.FileInfoDTO(" +
            "u.id, " +
            "u.filename, " +
            "u.fileSize, " +
            "u.createdAt, " +
            "u.expirationDate, " +
            "CASE WHEN u.filePassword IS NOT NULL AND u.filePassword != '' THEN true ELSE false END) " +
            "FROM FileData u WHERE u.email = :email")
    List<FileInfoDTO> findFilesByEmail(String email);

    @Query("SELECT f FROM FileData f WHERE f.expirationDate < :currentDate")
    List<FileData> findExpiredFiles(@Param("currentDate") LocalDateTime currentDate);

}
