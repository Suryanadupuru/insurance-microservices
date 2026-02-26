package com.sun.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — Single entry point for all Insurance Microservices.
 *
 * Responsibilities:
 *  - Route incoming requests to the correct downstream microservice
 *  - Validate JWT tokens before forwarding requests (via JwtAuthFilter)
 *  - Apply rate limiting per client IP / user
 *  - Circuit breaking for downstream failures
 *  - Load balancing across service instances (via Eureka)
 *  - CORS handling for frontend clients
 *
 * Port: 8080
 *
 * Route pattern:  /api/{service-name}/**  → lb://{service-name}
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
