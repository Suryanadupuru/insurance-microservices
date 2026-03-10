package com.sun.user_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * GatewayHeaderFilter — replaces JwtAuthenticationFilter entirely.
 *
 * The API Gateway validates JWT tokens and forwards identity as headers:
 *   X-User-Id   → authenticated user's DB id
 *   X-Username  → authenticated user's email
 *   X-User-Role → authenticated user's role (e.g. "USER" or "ADMIN")
 *
 * This filter reads those headers and populates the SecurityContext so that
 * Spring Security annotations like @PreAuthorize("hasRole('ADMIN')") work correctly.
 *
 * No JWT library is needed in user-service at all.
 */
@Slf4j
@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID   = "X-User-Id";
    private static final String HEADER_USERNAME  = "X-Username";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId   = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);
        String role     = request.getHeader(HEADER_USER_ROLE);

        // If gateway headers are present, set authentication in SecurityContext
        if (username != null && !username.isBlank() && role != null && !role.isBlank()) {

            // Build authority — Spring Security expects "ROLE_" prefix for hasRole()
            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,   // no credentials needed — gateway already verified
                            List.of(new SimpleGrantedAuthority(authority))
                    );

            // Store userId as details so controllers can access it without a DB call
            authentication.setDetails(userId);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("SecurityContext set for user: {}, role: {}", username, role);
        }

        filterChain.doFilter(request, response);
    }
}