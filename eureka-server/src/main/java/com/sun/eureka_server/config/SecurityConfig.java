package com.sun.eureka_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Eureka Server dashboard.
 * Enables basic authentication to protect the Eureka dashboard
 * while allowing Eureka client communication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // Disable CSRF for Eureka endpoints so clients can register
                .ignoringRequestMatchers("/eureka/**")
            )
            .authorizeHttpRequests(auth -> auth
                // Allow actuator health endpoints without auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // All other requests (dashboard, eureka API) require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {});  // Enable Basic Auth for dashboard access

        return http.build();
    }
}
