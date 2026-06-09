package com.openclassrooms.datashare.configuration.security;

import com.openclassrooms.datashare.configuration.security.SecurityConstants;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuration class for Spring Security settings.
 * This class defines security-related beans and configurations, including
 * authentication providers,
 * password encoding, and security filter chains. It integrates with JWT
 * authentication and CORS policies.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Configuring CORS policies using {@link CorsConfigurationSource}.</li>
 * <li>Disabling CSRF protection for stateless APIs.</li>
 * <li>Setting session management to stateless for JWT-based
 * authentication.</li>
 * <li>Defining public and private endpoints using
 * {@link SecurityConstants}.</li>
 * <li>Integrating JWT authentication filter for secure API access.</li>
 * <li>Handling authentication exceptions by returning HTTP 401 responses.</li>
 * </ul>
 *
 * @see SecurityConstants
 * @see CustomUserDetailService
 * @see JwtAuthenticationFilter
 * @see CorsConfigurationSource
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    /**
     * Custom user details service for loading user-specific data during
     * authentication.
     */
    @Autowired
    private CustomUserDetailService customUserDetailService;

    /**
     * JWT authentication filter for validating and processing JWT tokens in
     * incoming requests.
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * CORS configuration source for handling cross-origin requests.
     */
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    /**
     * Configures the authentication provider for Spring Security.
     * This provider uses {@link CustomUserDetailService} for loading user details
     * and
     * {@link BCryptPasswordEncoder} for password validation.
     *
     * @return A configured {@link DaoAuthenticationProvider} instance.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configures the authentication manager for Spring Security.
     *
     * @param authConfig The authentication configuration.
     * @return An {@link AuthenticationManager} instance.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures the password encoder for Spring Security.
     * This encoder uses BCrypt hashing for secure password storage.
     *
     * @return A {@link PasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain for HTTP requests.
     * This method defines:
     * <ul>
     * <li>CORS policies using {@link CorsConfigurationSource}.</li>
     * <li>CSRF protection disabled for stateless APIs.</li>
     * <li>Session management set to stateless for JWT-based authentication.</li>
     * <li>Public endpoints (e.g., registration, login) accessible without
     * authentication.</li>
     * <li>All other endpoints requiring authentication.</li>
     * <li>JWT authentication filter integration.</li>
     * <li>Authentication exception handling to return HTTP 401 responses.</li>
     * </ul>
     *
     * @param http The {@link HttpSecurity} instance to configure.
     * @return A configured {@link SecurityFilterChain} instance.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        // Actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        // Public routes (exact match, no path variables)
                        .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
                        // All other routes require authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(
                        (request, response, exception) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
                        }));
        return http.build();
    }

}
