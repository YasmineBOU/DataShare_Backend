package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.dto.RegisterDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.handler.exceptions.UserAlreadyExistsException;
import com.openclassrooms.datashare.repository.UserRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// @ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@Tag("UserServiceTest")
@DisplayName("Tests for UserService")
public class UserServiceTest {
    private static final String EMAIL = "john.doe@example.com";
    private static final String PASSWORD = "PASSWORD";
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService userService;

    // Register tests
    @Nested
    @Tag("register")
    @DisplayName("Tests for register method")
    class RegisterTests {

        @Test
        @DisplayName("Given a null user, when register is called, then IllegalArgumentException is thrown.")
        public void test_create_null_user_throws_IllegalArgumentException() {
            // GIVEN

            // THEN
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.register(null));
        }

        @Test
        @DisplayName("Given an existing user, when register is called, then IllegalArgumentException is thrown.")
        public void test_create_already_exist_user_throws_IllegalArgumentException() {
            // GIVEN
            User existingUser = new User();
            existingUser.setEmail(EMAIL);
            existingUser.setPassword(PASSWORD);
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(existingUser));

            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setEmail(EMAIL);
            registerDTO.setPassword(PASSWORD);

            // THEN
            Assertions.assertThrows(
                    UserAlreadyExistsException.class,
                    () -> userService.register(registerDTO));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given a valid user, when register is called, then the user is saved.")
        public void test_create_user() {
            // GIVEN
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setEmail(EMAIL);
            registerDTO.setPassword(PASSWORD);
            when(passwordEncoder.encode(PASSWORD)).thenReturn("ENCODED_PASSWORD");
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            // WHEN
            userService.register(registerDTO);

            // THEN
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            verify(passwordEncoder, times(1)).encode(PASSWORD);
            assertThat(userCaptor.getValue().getEmail()).isEqualTo(EMAIL);
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("ENCODED_PASSWORD");
        }
    }

    // Login tests
    @Nested
    @Tag("login")
    @DisplayName("Tests for login method")
    class LoginTests {
        @Test
        @DisplayName("Given a null login, when login is called, then IllegalArgumentException is thrown.")
        public void test_login_with_null_login_throws_IllegalArgumentException() {
            // THEN
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.login(null, PASSWORD));
        }

        @Test
        @DisplayName("Given invalid credentials, when login is called, then BadCredentialsException is thrown.")
        public void test_login_with_invalid_credentials_throws_BadCredentialsException() {
            // GIVEN
            User existingUser = new User();
            existingUser.setEmail(EMAIL);
            existingUser.setPassword(PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("WRONG_PASSWORD",
                    existingUser.getPassword())).thenReturn(false);

            // THEN
            Assertions.assertThrows(
                    BadCredentialsException.class,
                    () -> userService.login(EMAIL, "WRONG_PASSWORD"));

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Given a user that is not found, when login is called, then BadCredentialsException is thrown.")
        public void test_login_with_user_not_found_throws_BadCredentialsException() {
            // GIVEN
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            // THEN
            Assertions.assertThrows(
                    BadCredentialsException.class,
                    () -> userService.login(EMAIL, PASSWORD));

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Given valid credentials, when login is called, then a JWT token is returned.")
        public void test_login_with_valid_credentials_returns_jwt_token() {
            // GIVEN
            User existingUser = new User();
            existingUser.setEmail(EMAIL);
            existingUser.setPassword(PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(PASSWORD, existingUser.getPassword())).thenReturn(true);
            when(jwtService.generateToken(any())).thenReturn("mocked-jwt-token");

            // WHEN
            String token = userService.login(EMAIL, PASSWORD);

            // THEN
            verify(userRepository, times(1)).findByEmail(EMAIL);
            verify(jwtService, times(1)).generateToken(any());
            assertThat(token).isEqualTo("mocked-jwt-token");
        }
    }

}
