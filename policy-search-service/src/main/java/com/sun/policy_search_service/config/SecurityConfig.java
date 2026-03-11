package com.sun.policy_search_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sun.policy_search_service.filter.GatewayHeaderFilter;

import lombok.RequiredArgsConstructor;

/**
 * All policy search endpoints are publicly accessible.
 *  "The user is logged in or browsing as a guest."
 *
 * GatewayHeaderFilter still runs so that if a logged-in user
 * calls the search, their identity is available if needed later
 * (e.g. for personalisation or analytics).
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final GatewayHeaderFilter gatewayHeaderFilter;
	
	
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> 
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll() // all search endpoints are public
			)
			.addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
		
}
