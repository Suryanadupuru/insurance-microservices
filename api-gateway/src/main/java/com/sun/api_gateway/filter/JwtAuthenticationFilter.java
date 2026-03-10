package com.sun.api_gateway.filter;

import com.sun.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global JWT Authentication Filter.
 *
 * Runs on EVERY request through the gateway.
 * Skips authentication for public endpoints (login, register, quotes).
 * For protected endpoints, validates the Bearer token and forwards
 * user info as headers to downstream services.
 *
 * Downstream services can trust X-User-Id, X-Username, X-User-Role headers
 * since they are added only by this gateway after validation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        log.debug("Gateway received request: {} {}", request.getMethod(), path);

        // Skip JWT check for public endpoints
        if (routeValidator.isOpenEndpoint(request)) {
            log.debug("Open endpoint, skipping JWT check: {}", path);
            return chain.filter(exchange);
        }

        // Check Authorization header
        if (!request.getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
            log.warn("Missing Authorization header for: {}", path);
            return unauthorized(exchange, "Authorization header is required");
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization format for: {}", path);
            return unauthorized(exchange, "Authorization must start with 'Bearer '");
        }

        String token = authHeader.substring(7);  // Strip "Bearer "

        // Validate the token
        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid or expired JWT token for: {}", path);
            return unauthorized(exchange, "Invalid or expired token");
        }

        // Extract user info and forward as headers to downstream services
        String username = jwtUtil.extractUsername(token);
        String role     = jwtUtil.extractRole(token);
        String userId   = jwtUtil.extractUserId(token);

        log.debug("JWT valid for user: {}, role: {}", username, role);

        // Mutate request to add downstream headers
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id",   userId   != null ? userId   : "")
                .header("X-Username",  username != null ? username : "")
                .header("X-User-Role", role     != null ? role     : "")
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // Run before all other filters (lower = higher priority)
        return -1;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = """
                {"status":401,"error":"Unauthorized","message":"%s"}
                """.formatted(message);
        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
