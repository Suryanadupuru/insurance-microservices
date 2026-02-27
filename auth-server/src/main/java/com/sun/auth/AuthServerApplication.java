package com.sun.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Auth Server — Handles all authentication and authorization.
 *
 * Endpoints:
 *  POST /api/auth/register       — create new user account
 *  POST /api/auth/login          — returns accessToken + refreshToken
 *  POST /api/auth/refresh-token  — exchange refreshToken for new accessToken
 *  POST /api/auth/logout         — invalidate refresh token
 *  GET  /api/auth/validate       — validate token (used by other services)
 *
 * Port: 8090
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
