package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.dto.RegisterDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.entities.UserRoleEnum;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.service.JwtService;
import com.openclassrooms.datashare.service.UserService;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
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
            User user = new User();
            user.setFirstName(FIRST_NAME);
            user.setLastName(LAST_NAME);
            user.setEmail(EMAIL);
            user.setPassword(PASSWORD);
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

            // THEN
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.register(user));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given a valid user, when register is called, then the user is saved.")
        public void test_create_user() {
            // GIVEN
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setFirstName(FIRST_NAME);
            registerDTO.setLastName(LAST_NAME);
            registerDTO.setLogin(LOGIN);
            registerDTO.setPassword(PASSWORD);
            when(passwordEncoder.encode(PASSWORD)).thenReturn("ENCODED_PASSWORD");
            when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.empty());

            // WHEN
            userService.register(registerDTO);

            // THEN
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            verify(passwordEncoder, times(1)).encode(PASSWORD);
            assertThat(userCaptor.getValue()).isSameAs(user);
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
            User user = new User();
            user.setFirstName(FIRST_NAME);
            user.setLastName(LAST_NAME);
            user.setLogin(LOGIN);
            user.setPassword(PASSWORD);
            when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("WRONG_PASSWORD",
                    user.getPassword())).thenReturn(false);

            // THEN
            Assertions.assertThrows(
                    BadCredentialsException.class,
                    () -> userService.login(LOGIN, "WRONG_PASSWORD"));

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Given a user that is not found, when login is called, then BadCredentialsException is thrown.")
        public void test_login_with_user_not_found_throws_BadCredentialsException() {
            // GIVEN
            when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.empty());

            // THEN
            Assertions.assertThrows(
                    BadCredentialsException.class,
                    () -> userService.login(LOGIN, PASSWORD));

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Given valid credentials, when login is called, then a JWT token is returned.")
        public void test_login_with_valid_credentials_returns_jwt_token() {
            // GIVEN
            User user = new User();
            user.setFirstName(FIRST_NAME);
            user.setLastName(LAST_NAME);
            user.setLogin(LOGIN);
            user.setPassword(PASSWORD);
            when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, user.getPassword())).thenReturn(true);
            when(jwtService.generateToken(any())).thenReturn("mocked-jwt-token");

            // WHEN
            String token = userService.login(LOGIN, PASSWORD);

            // THEN
            verify(userRepository, times(1)).findByLogin(LOGIN);
            verify(jwtService, times(1)).generateToken(any());
            assertThat(token).isEqualTo("mocked-jwt-token");
        }
    }

    // Add user tests
    @Nested
    @Tag("addUser")
    @DisplayName("Tests for addUser method")
    class AddUserTests {
        userToUpdate.setFirstName("NewFirstName");

        User existingUser = new User();existingUser.setFirstName(FIRST_NAME);existingUser.setLastName(LAST_NAME);existingUser.setLogin(LOGIN);existingUser.setPassword(PASSWORD);existingUser.setRole(ROLE);

        when(userRepository.findById(existingId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            userService.updateUser(new User(), existingId, userToUpdate);

            // THEN
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFirstName()).isEqualTo("NewFirstName");
        }

        @Test
        @DisplayName("Given a valid existing id and new lastName, when updateUser is called, then the lastName is updated.")
        public void test_updateUser_with_valid_existing_id_and_new_lastName_updates_lastName() {
            // GIVEN
            long existingId = 1L;
            User userToUpdate = new User();
            userToUpdate.setLastName("NewLastName");

            User existingUser = new User();
            existingUser.setFirstName(FIRST_NAME);
            existingUser.setLastName(LAST_NAME);
            existingUser.setLogin(LOGIN);
            existingUser.setPassword(PASSWORD);
            existingUser.setRole(ROLE);

            when(userRepository.findById(existingId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            userService.updateUser(new User(), existingId, userToUpdate);

            // THEN
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getLastName()).isEqualTo("NewLastName");
        }

        @Test
        @DisplayName("Given a valid existing id and a new login already used by another user, when updateUser is called, then DataIntegrityViolationException is thrown.")
        public void test_updateUser_with_duplicate_login_throws_DataIntegrityViolationException() {
            // GIVEN
            long existingId = 1L;
            User userToUpdate = new User();
            userToUpdate.setLogin("existingLogin"); // Login déjà utilisé par un autre utilisateur

            User existingUser = new User();
            existingUser.setFirstName(FIRST_NAME);
            existingUser.setLastName(LAST_NAME);
            existingUser.setLogin(LOGIN);
            existingUser.setPassword(PASSWORD);
            existingUser.setRole(ROLE);

            when(userRepository.findById(existingId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class)))
                    .thenThrow(new DataIntegrityViolationException("Login already exists"));

            // THEN
            Assertions.assertThrows(
                    DataIntegrityViolationException.class,
                    () -> userService.updateUser(new User(), existingId, userToUpdate));
        }

        @Test
        @DisplayName("Given a valid existing id and new password, when updateUser is called, then the password is updated.")
        public void test_updateUser_with_valid_existing_id_and_new_password_updates_password() {
            // GIVEN
            long existingId = 1L;
            User userToUpdate = new User();
            userToUpdate.setPassword("NewPassword");

            User existingUser = new User();
            existingUser.setFirstName(FIRST_NAME);
            existingUser.setLastName(LAST_NAME);
            existingUser.setLogin(LOGIN);
            existingUser.setPassword("OldPassword");
            existingUser.setRole(ROLE);

            when(userRepository.findById(existingId)).thenReturn(Optional.of(existingUser));

            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(user.getPassword().trim()));
                }
                return user;
            });

            // WHEN
            userService.updateUser(new User(), existingId, userToUpdate);

            // THEN
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo(passwordEncoder.encode("NewPassword"));
        }

    }

}
