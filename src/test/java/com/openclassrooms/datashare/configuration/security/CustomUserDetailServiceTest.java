package com.openclassrooms.datashare.configuration.security;

import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("CustomUserDetailServiceTest")
@DisplayName("Tests for CustomUserDetailService")
class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @Test
    @DisplayName("Given an existing email, when loadUserByUsername is called, then matching user details are returned.")
    void test_loadUserByUsername_withExistingEmail_returnsUserDetails() {
        // GIVEN
        String email = "existingUser@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded-password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // WHEN
        UserDetails result = customUserDetailService.loadUserByUsername(email);

        // THEN
        assertSame(user, result);
        assertEquals(email, result.getUsername());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Given an unknown email, when loadUserByUsername is called, then UsernameNotFoundException is thrown.")
    void test_loadUserByUsername_withUnknownEmail_throwsUsernameNotFoundException() {
        // GIVEN
        String email = "unknownUser@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // WHEN
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailService.loadUserByUsername(email));

        // THEN
        assertEquals("User Not Found with email: " + email, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }

}
