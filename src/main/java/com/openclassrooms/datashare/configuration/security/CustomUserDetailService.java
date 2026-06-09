package com.openclassrooms.datashare.configuration.security;

import com.openclassrooms.datashare.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of {@link UserDetailsService} for loading user-specific
 * data in Spring Security.
 * This service interacts with the {@link UserRepository} to fetch user details
 * by email and integrates
 * with Spring Security's authentication mechanism.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Loading user details from the database using the user's email.</li>
 * <li>Throwing a {@link UsernameNotFoundException} if the user is not
 * found.</li>
 * </ul>
 *
 * @see UserDetailsService
 * @see UserRepository
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    /**
     * Repository for accessing user data from the database.
     */
    private final UserRepository userRepository;

    /**
     * Loads user details by email for Spring Security authentication.
     *
     * @param email The email of the user to load.
     * @return A {@link UserDetails} object representing the user.
     * @throws UsernameNotFoundException If no user is found with the provided
     *                                   email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
    }

}
