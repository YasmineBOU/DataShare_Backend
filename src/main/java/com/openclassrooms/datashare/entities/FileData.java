package com.openclassrooms.datashare.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing a file stored in the system.
 * This class stores metadata about files uploaded by users, including their
 * storage location,
 * expiration date, access control, and timestamps for creation and updates.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Storing file metadata such as filename, size, type, and hash.</li>
 * <li>Tracking file storage location (fileKey, fileLink, fileToken).</li>
 * <li>Managing file expiration and deletion status.</li>
 * <li>Tracking timestamps for file creation and updates.</li>
 * </ul>
 */
@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileData {
    /**
     * Unique identifier for the file.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * User who uploaded the file (optional association).
     * This allows for tracking files uploaded by registered users while still
     * supporting anonymous uploads.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true) // nullable = true for anonymous uploads
    private User user;

    /**
     * Timestamp indicating when the file was created.
     * Automatically set by Hibernate using {@link CreationTimestamp}.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Name of the file.
     */
    @NotBlank
    @Column(name = "filename", nullable = false)
    private String filename;

    /**
     * Unique key for the file stored in Backblaze B2.
     * This field is required and cannot be blank.
     */
    @NotBlank
    @Column(name = "file_key", nullable = false)
    private String fileKey;

    /**
     * Optional password for accessing the file.
     */
    @Column(name = "file_password")
    private String filePassword;

    /**
     * Date and time when the file will expire.
     * This field is required and cannot be null.
     */
    @NotNull
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    /**
     * Hash of the file content for integrity checks.
     * This field is required and cannot be blank.
     */
    @NotBlank
    @Column(name = "hash", nullable = false)
    private String hash;

    /**
     * Link to access the file in Backblaze B2.
     * Stored as TEXT to accommodate long URLs.
     * This field is required and cannot be blank.
     */
    @NotBlank
    @Column(name = "file_link", nullable = false, columnDefinition = "TEXT")
    private String fileLink;

    /**
     * Unique token for the file, used for secure access.
     * Stored as TEXT to accommodate long tokens.
     */
    @Column(name = "file_token", columnDefinition = "TEXT")
    private String fileToken;

    /**
     * Size of the file in bytes.
     * This field is required and cannot be null.
     */
    @NotNull
    @Column(name = "file_size", nullable = false)
    private long fileSize;

    /**
     * Type of the file (e.g., "image/png", "application/pdf").
     * This field is required and cannot be blank.
     */
    @NotBlank
    @Column(name = "file_type", nullable = false)
    private String fileType;

    /**
     * Timestamp indicating when the file was last updated.
     * Automatically set by Hibernate using {@link UpdateTimestamp}.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Indicates whether the file has been deleted.
     * Defaults to false (not deleted).
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    /**
     * Retrieves the email of the user who uploaded the file.
     *
     * @return The email of the user, or null if the file was uploaded anonymously.
     */
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

}
