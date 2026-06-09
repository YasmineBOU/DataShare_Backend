package com.openclassrooms.datashare.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.openclassrooms.datashare.service.JwtService;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT authentication filter for validating and processing JWT tokens in
 * incoming requests.
 * This filter extends {@link OncePerRequestFilter} and runs once per request to
 * authenticate users
 * based on JWT tokens stored in either the Authorization header or an HttpOnly
 * cookie.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Extracting JWT tokens from the Authorization header or HttpOnly
 * cookie.</li>
 * <li>Validating the token using {@link JwtService}.</li>
 * <li>Authenticating the user and setting the authentication in the security
 * context.</li>
 * <li>Handling invalid or expired tokens by returning HTTP 401 responses.</li>
 * </ul>
 *
 * @see OncePerRequestFilter
 * @see JwtService
 * @see UserRepository
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Logger for logging authentication events and errors.
     */
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * Repository for accessing user data from the database.
     */
    private final UserRepository userRepository;

    /**
     * Service for validating JWT tokens and extracting user information.
     */
    private final JwtService jwtService;

    /**
     * Processes the incoming HTTP request to validate the JWT token and
     * authenticate the user.
     *
     * <p>
     * Steps performed:
     * <ol>
     * <li>Extracts the JWT token from the Authorization header (if present).</li>
     * <li>If not found in the header, extracts the token from an HttpOnly
     * cookie.</li>
     * <li>Validates the token using {@link JwtService}.</li>
     * <li>If the token is valid, authenticates the user and sets the authentication
     * in the security context.</li>
     * <li>Handles invalid or expired tokens by returning HTTP 401 responses.</li>
     * </ol>
     *
     * @param request     The HTTP request.
     * @param response    The HTTP response.
     * @param filterChain The filter chain to continue processing the request.
     * @throws ServletException If an error occurs during request processing.
     * @throws IOException      If an I/O error occurs during request processing.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String jwtToken = null;
        String usernameTemp = null;

        // Try to get token from Authorization header first (for backward compatibility)
        final String authHeader = request.getHeader("Authorization");
        final String tokenPrefix = "Bearer ";

        if (authHeader != null && authHeader.startsWith(tokenPrefix)) {
            jwtToken = authHeader.substring(tokenPrefix.length());
        }

        // If not found in header, try to get token from HttpOnly cookie
        if (jwtToken == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (SecurityConstants.AUTH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                        log.debug("Token found in HttpOnly cookie");
                        break;
                    }
                }
            }
        }

        // Validate token and extract username
        if (jwtToken != null) {
            try {
                usernameTemp = jwtService.validateTokenAndGetUsername(jwtToken);
            } catch (RuntimeException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired JWT token");
                return;
            }
        }

        // If username extracted and authentication is absent or anonymous, authenticate
        // user
        final String username = usernameTemp;
        var currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (username != null && (currentAuth == null || currentAuth instanceof AnonymousAuthenticationToken)) {

            log.debug("Authenticating user: {}", username);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    null);

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authenticated user in security context
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}
