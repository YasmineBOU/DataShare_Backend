package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.dto.AuthDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Service responsible for managing user-related operations such as registration
 * and authentication.
 * This service interacts with {@link UserRepository} to persist user data and
 * {@link PasswordEncoder}
 * to securely hash passwords. It also generates JWT tokens for authenticated
 * users via {@link JwtService}.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Registering new users with email and password.</li>
 * <li>Authenticating users and generating JWT tokens for secure access.</li>
 * </ul>
 *
 * @see UserRepository
 * @see PasswordEncoder
 * @see JwtService
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new user to the database.
     * 
     * @param authDTO the user to be registered
     * @throws IllegalArgumentException if a user with the same login already exists
     */
    public void register(AuthDTO authDTO) {
        Assert.notNull(authDTO, "Auth data must not be null");

        Optional<User> optionalUser = userRepository.findByEmail(authDTO.getEmail());
        if (optionalUser.isPresent()) {
            throw new IllegalArgumentException("User with email " + authDTO.getEmail() + " already exists");
        }
        User user = new User();
        user.setEmail(authDTO.getEmail());
        user.setPassword(passwordEncoder.encode(authDTO.getPassword()));
        userRepository.save(user);
    }

    /**
     * Login with given credentials and return a JWT token.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return a JWT token if the credentials are valid
     * @throws IllegalArgumentException if the credentials are invalid
     */
    public String login(String email, String password) {
        Assert.notNull(email, "Email must not be null");
        Assert.notNull(password, "Password must not be null");
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.get().getEmail())
                    .password(user.get().getPassword())
                    .build();
            return jwtService.generateToken(userDetails);
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
