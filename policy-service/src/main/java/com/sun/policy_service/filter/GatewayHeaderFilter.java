package com.sun.policy_service.filter;

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

/**
 * Reads gateway-forwarded identity headers and populates SecurityContext.
 * Same pattern used across all downstream services.
 *
 * Headers set by API Gateway after JWT validation:
 *   X-User-Id   → user's DB id
 *   X-Username  → user's email
 *   X-User-Role → user's role (USER / ADMIN / AGENT)
 */

@Slf4j
@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {
	
	private static final String HEADER_USER_ID = "X-User-Id";
	private static final String HEADER_USERNAME = "X-Username";
	private static final String HEADER_USER_ROLE = "X-User-Role";
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) 
				throws ServletException, IOException {
		String userId = request.getHeader(HEADER_USER_ID);
		String username = request.getHeader(HEADER_USERNAME);
		String userRole = request.getHeader(HEADER_USER_ROLE);
		
		if (username != null && !username.isBlank() && userRole != null && !userRole.isBlank()) {
			String authority = userRole.startsWith("ROLE_") ? userRole : "ROLE_" + userRole;
			
			
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
					username, 
					null,
					List.of(new SimpleGrantedAuthority(authority)));
			
			authToken.setDetails(userId); // Store userId in details for later retrieval
			SecurityContextHolder.getContext().setAuthentication(authToken);
			
			log.debug("SecurityContext set for user: {}, role: {}", username, userRole);
			
		} else {
			log.warn("Gateway headers missing: userId={}, username={}, role={}", userId, username, userRole);
		}
		
		filterChain.doFilter(request, response);
	}

}
