package com.openclassrooms.datashare.repository;

import com.openclassrooms.datashare.entities.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link User} entities in the database.
 * This interface provides CRUD operations and custom queries for user-related
 * data,
 * including finding users by their email address.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Finding a user by their email address.</li>
 * </ul>
 *
 * @see User
 * @see JpaRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their email address.
     *
     * @param email The email address of the user to find.
     * @return An {@link Optional} containing the {@link User} entity if found, or
     *         empty otherwise.
     */
    Optional<User> findByEmail(String email);
}
