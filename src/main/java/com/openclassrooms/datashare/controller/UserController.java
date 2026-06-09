package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.configuration.security.SecurityConstants;
import com.openclassrooms.datashare.dto.AuthDTO;
import com.openclassrooms.datashare.dto.AuthMeDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing user authentication and authorization.
 * This controller handles user registration, login, logout, and profile
 * retrieval.
 * It interacts with {@link UserService} for business logic and uses JWT tokens
 * stored in HTTP-only cookies
 * for secure session management.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Registering new users via the <code>/register</code> endpoint.</li>
 * <li>Authenticating users and setting a JWT token in an HTTP-only cookie via
 * the <code>/login</code> endpoint.</li>
 * <li>Retrieving the authenticated user's profile via the <code>/auth/me</code>
 * endpoint.</li>
 * <li>Logging out users by clearing the JWT token cookie via the
 * <code>/logout</code> endpoint.</li>
 * </ul>
 *
 * @see UserService
 * @see AuthDTO
 * @see AuthMeDTO
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    /**
     * Service for handling user-related business logic.
     */
    private final UserService userService;

    /**
     * Indicates whether the JWT token cookie should be marked as secure.
     * If true, the cookie is only sent over HTTPS.
     */
    @Value("${com.openclassrooms.datashare.cookie.secure:true}")
    private boolean cookieSecure;

    /**
     * Registers a new user.
     *
     * @param authDTO The user registration data (email and password).
     * @return A {@link ResponseEntity} with HTTP status 201 (Created) if the
     *         registration is successful.
     * @throws IllegalArgumentException If the email is already registered or the
     *                                  password is invalid.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTO authDTO) {
        userService.register(authDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and sets a JWT token in an HTTP-only cookie.
     *
     * @param authDTO  The user login data (email and password).
     * @param response The HTTP servlet response to set the cookie.
     * @return A {@link ResponseEntity} with a success message if the login is
     *         successful.
     * @throws IllegalArgumentException If the email or password is invalid.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTO authDTO, HttpServletResponse response) {
        String jwtToken = userService.login(authDTO.getEmail(), authDTO.getPassword());

        ResponseCookie cookie = ResponseCookie.from(SecurityConstants.AUTH_TOKEN_COOKIE_NAME, jwtToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("None")
                .path("/")
                .maxAge(SecurityConstants.AUTH_COOKIE_MAX_AGE_SECONDS)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("message", "Logged successfully !"));
    }

    /**
     * Retrieves the profile of the authenticated user.
     *
     * @return A {@link ResponseEntity} containing an {@link AuthMeDTO} with:
     *         <ul>
     *         <li><code>true</code> and the user's email if the user is
     *         authenticated.</li>
     *         <li><code>false</code> and <code>null</code> if the user is not
     *         authenticated.</li>
     *         </ul>
     */
    @GetMapping("/auth/me")
    public ResponseEntity<AuthMeDTO> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthMeDTO(false, null));
        }

        return ResponseEntity.ok(new AuthMeDTO(true, user.getEmail()));
    }

    /**
     * Logs out the user by clearing the JWT token cookie.
     *
     * @param response The HTTP servlet response to clear the cookie.
     * @return A {@link ResponseEntity} with a success message if the logout is
     *         successful.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(SecurityConstants.AUTH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully !"));
    }
}
