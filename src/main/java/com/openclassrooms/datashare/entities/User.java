package com.openclassrooms.datashare.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Entity representing a user in the system.
 * This class implements {@link UserDetails} to integrate with Spring Security
 * for authentication and authorization.
 * It stores user information such as email, password, and timestamps for
 * creation and updates.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Storing user credentials (email and password).</li>
 * <li>Providing Spring Security integration via {@link UserDetails}.</li>
 * <li>Tracking creation and update timestamps automatically.</li>
 * </ul>
 *
 * @see UserDetails
 */
@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * User's email address. This field is unique and cannot be blank.
     */
    @NotBlank
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /**
     * User's password. This field cannot be blank.
     */
    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Timestamp indicating when the user was created.
     * Automatically set by Hibernate using {@link CreationTimestamp}.
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at;

    /**
     * Timestamp indicating when the user was last updated.
     * Automatically set by Hibernate using {@link UpdateTimestamp}.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    /**
     * Returns the authorities granted to the user.
     * In this implementation, the user has no authorities (empty list).
     *
     * @return A collection of {@link GrantedAuthority} objects.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    /**
     * Returns the username used for authentication.
     * In this implementation, the username is the user's email.
     *
     * @return The username (email) of the user.
     */
    @Override
    public String getUsername() {
        return email;
    }
}
