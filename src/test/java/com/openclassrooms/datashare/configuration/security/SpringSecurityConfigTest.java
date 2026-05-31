package com.openclassrooms.datashare.configuration.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for SpringSecurityConfig")
public class SpringSecurityConfigTest {

    @Mock
    private CustomUserDetailService customUserDetailService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private CorsConfigurationSource corsConfigurationSource;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    private SpringSecurityConfig springSecurityConfig;

    @BeforeEach
    void setUp() {
        springSecurityConfig = new SpringSecurityConfig();
        ReflectionTestUtils.setField(springSecurityConfig, "customUserDetailService", customUserDetailService);
        ReflectionTestUtils.setField(springSecurityConfig, "jwtAuthenticationFilter", jwtAuthenticationFilter);
        ReflectionTestUtils.setField(springSecurityConfig, "corsConfigurationSource", corsConfigurationSource);
    }

    @Test
    @DisplayName("passwordEncoder() should return a BCryptPasswordEncoder")
    void password_encoder_should_be_bcrypt() {
        PasswordEncoder passwordEncoder = springSecurityConfig.passwordEncoder();

        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("authenticationProvider() should return a DaoAuthenticationProvider")
    void authentication_provider_should_be_a_dao_authentication_provider() {
        DaoAuthenticationProvider authenticationProvider = springSecurityConfig.authenticationProvider();

        assertThat(authenticationProvider).isNotNull();
        assertThat(authenticationProvider).isInstanceOf(DaoAuthenticationProvider.class);
    }

    @Test
    @DisplayName("authenticationManager() should delegate to AuthenticationConfiguration")
    void authentication_manager_should_delegate_to_authentication_configuration() throws Exception {
        AuthenticationManager expectedAuthenticationManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(expectedAuthenticationManager);

        AuthenticationManager authenticationManager = springSecurityConfig
                .authenticationManager(authenticationConfiguration);

        assertThat(authenticationManager).isSameAs(expectedAuthenticationManager);
    }
}