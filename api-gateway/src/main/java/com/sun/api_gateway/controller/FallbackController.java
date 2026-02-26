package com.sun.api_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * FallbackController — returns friendly error responses when a
 * downstream service is unavailable (circuit breaker is open).
 *
 * Routes in application.yml point here via:
 *   filters:
 *     - name: CircuitBreaker
 *       args:
 *         fallbackUri: forward:/fallback/{service}
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return fallbackResponse("user-service");
    }

    @GetMapping("/policy-service")
    public Mono<ResponseEntity<Map<String, Object>>> policyServiceFallback() {
        return fallbackResponse("policy-service");
    }

    @GetMapping("/policy-search-service")
    public Mono<ResponseEntity<Map<String, Object>>> policySearchFallback() {
        return fallbackResponse("policy-search-service");
    }

    @GetMapping("/application-service")
    public Mono<ResponseEntity<Map<String, Object>>> applicationServiceFallback() {
        return fallbackResponse("application-service");
    }

    @GetMapping("/claims-service")
    public Mono<ResponseEntity<Map<String, Object>>> claimsServiceFallback() {
        return fallbackResponse("claims-service");
    }

    @GetMapping("/claims-tracking-service")
    public Mono<ResponseEntity<Map<String, Object>>> claimsTrackingFallback() {
        return fallbackResponse("claims-tracking-service");
    }

    @GetMapping("/renewal-service")
    public Mono<ResponseEntity<Map<String, Object>>> renewalServiceFallback() {
        return fallbackResponse("renewal-service");
    }

    @GetMapping("/quote-service")
    public Mono<ResponseEntity<Map<String, Object>>> quoteServiceFallback() {
        return fallbackResponse("quote-service");
    }

    @GetMapping("/support-service")
    public Mono<ResponseEntity<Map<String, Object>>> supportServiceFallback() {
        return fallbackResponse("support-service");
    }

    @GetMapping("/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback() {
        return fallbackResponse("notification-service");
    }

    @GetMapping("/analytics-service")
    public Mono<ResponseEntity<Map<String, Object>>> analyticsServiceFallback() {
        return fallbackResponse("analytics-service");
    }

    // ─── Shared fallback builder ──────────────────────────────────────────────

    private Mono<ResponseEntity<Map<String, Object>>> fallbackResponse(String serviceName) {
        log.warn("Circuit breaker triggered — {} is unavailable", serviceName);
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",    HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error",     "Service Unavailable",
                "message",   String.format(
                        "The %s is currently unavailable. Please try again in a few moments.",
                        serviceName),
                "service",   serviceName
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
