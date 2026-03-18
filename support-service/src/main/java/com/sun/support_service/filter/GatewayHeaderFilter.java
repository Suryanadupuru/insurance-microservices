package com.sun.support_service.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GatewayHeaderFilter extends OncePerRequestFilter{

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
 
        if (username != null && !username.isBlank() && role != null && !role.isBlank()) {
            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
 
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username, null,
                            List.of(new SimpleGrantedAuthority(authority))
                    );
            authentication.setDetails(userId);
            SecurityContextHolder.getContext().setAuthentication(authentication);
 
            log.debug("SecurityContext set for user: {}, role: {}", username, role);
        }
 
        filterChain.doFilter(request, response);
    }
}
