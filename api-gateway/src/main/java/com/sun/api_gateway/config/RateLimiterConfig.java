package com.sun.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate Limiter Configuration.
 *
 * Defines how rate limiting keys are resolved.
 * Currently uses client IP address as the key so that each IP
 * is rate-limited independently.
 *
 * Can be swapped to user-based key resolution once auth is integrated.
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Rate limit by client IP address.
     * Falls back to "anonymous" if IP cannot be determined.
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "anonymous";
            return Mono.just(ip);
        };
    }

    /**
     * Rate limit by authenticated user (username from JWT header).
     * Use this after JWT filter has forwarded X-Username header.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String username = exchange.getRequest().getHeaders().getFirst("X-Username");
            return Mono.just(username != null ? username : "anonymous");
        };
    }
}
