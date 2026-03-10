package com.sun.api_gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * RouteValidator — defines which API endpoints are open (public) and
 * do NOT require JWT authentication.
 *
 * All other routes are secured and require a valid Bearer token.
 */
@Component
public class RouteValidator {

    /**
     * List of endpoint patterns that are accessible without a JWT token.
     * These are typically: login, register, guest quote browsing.
     */
    public static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh-token",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/users/register",
            "/api/quotes/guest",           // Guests can browse quotes without login
            "/api/policy-search/public",   // Guests can search public policies
            "/actuator/health",
            "/actuator/info"
    );

    /**
     * Predicate — returns true if the request path matches any open endpoint.
     */
    public Predicate<ServerHttpRequest> isOpenEndpointPredicate() {
        return request -> OPEN_ENDPOINTS.stream()
                .anyMatch(uri -> request.getURI().getPath().startsWith(uri));
    }

    /**
     * Convenience method — returns true if the request does NOT need JWT auth.
     */
    public boolean isOpenEndpoint(ServerHttpRequest request) {
        return isOpenEndpointPredicate().test(request);
    }
}
