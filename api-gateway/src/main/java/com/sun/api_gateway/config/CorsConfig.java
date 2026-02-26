package com.sun.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS Configuration for the API Gateway.
 *
 * Centralizes CORS handling at the gateway level so individual
 * microservices do not need to configure it themselves.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allowed origins — tighten this in production to your actual frontend domain
        corsConfig.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",    // React / Vue dev server
                "http://localhost:4200",    // Angular dev server
                "https://*.insurance.com"  // Production frontend domains
        ));

        corsConfig.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        corsConfig.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        corsConfig.setExposedHeaders(List.of(
                "Authorization",
                "X-Total-Count"
        ));

        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);  // Cache preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
