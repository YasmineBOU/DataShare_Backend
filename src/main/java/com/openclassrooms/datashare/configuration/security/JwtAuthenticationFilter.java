package com.openclassrooms.datashare.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {

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
