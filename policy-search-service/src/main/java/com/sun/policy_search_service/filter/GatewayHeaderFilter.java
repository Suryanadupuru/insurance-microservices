package com.sun.policy_search_service.filter;

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
 * Standard pattern across all downstream services.
 */

@Slf4j
@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {
	
	private static final String HEADER_USER_ID   = "X-User-Id";
	private static final String HEADER_USERNAME  = "X-Username";
	private static final String HEADER_USER_ROLES = "X-User-Roles";

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain)
			throws IOException, ServletException {
		
		// Extract user info from headers set by the API Gateway (if present)
		String userId = request.getHeader(HEADER_USER_ID);
		String username = request.getHeader(HEADER_USERNAME);
		String userRoles = request.getHeader(HEADER_USER_ROLES);
		
		// For demonstration, we just log the extracted info.
		// In a real application, you might set this in a security context or thread-local for later use.
		if (username != null && !username.isBlank() && userRoles != null && !userRoles.isBlank()) {
			
			String authority = userRoles.startsWith("ROLE_") ? userRoles : "ROLE_" + userRoles;
			
			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
							username,
							null,   // no credentials needed — gateway already verified
							List.of(new SimpleGrantedAuthority(authority))
					);
			authentication.setDetails(userId); // store userId in details for later retrieval if needed
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			log.debug("SecurityContext set for user: {}, role: {}", username, userRoles);
		}
		// Continue with the filter chain
		filterChain.doFilter(request, response);
	}

}
