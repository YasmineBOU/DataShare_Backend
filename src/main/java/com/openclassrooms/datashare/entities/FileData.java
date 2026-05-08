package com.openclassrooms.datashare.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Optional association to User
    @Column(name = "email")
    private String email;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NotBlank
    @Column(name = "filename", nullable = false)
    private String filename;

    // Unique filename with UUID stored in Backblaze B2
    @NotBlank
    @Column(name = "file_key", nullable = false)
    private String fileKey;

    // Optional password for file access
    @Column(name = "file_password")
    private String filePassword;

    @NotNull
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @NotBlank
    @Column(name = "hash", nullable = false)
    private String hash;

    @NotBlank
    @Column(name = "file_link", nullable = false, columnDefinition = "TEXT")
    private String fileLink;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @NotBlank
    @Column(name = "file_type", nullable = false)
    private String fileType;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
