package com.sun.user_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 * Satisfies BRD NFR 5.8 — API Documentation.
 *
 * Access Swagger UI at: http://localhost:8081/swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Insurance Platform — User Service API",
        version = "1.0.0",
        description = """
            REST API for User Registration, Authentication, and Profile Management.
            
            **Use Case 1 (BRD):** User Registration with email verification.
            
            **Authentication Flow:**
            1. Register via `POST /api/auth/register`
            2. Verify email via link sent to inbox
            3. Login via `POST /api/auth/login` to receive Bearer token
            4. Pass `Authorization: Bearer <token>` header on all protected calls
            5. Refresh token via `POST /api/auth/refresh` before expiry
            """,
        contact = @Contact(
            name = "Insurance Platform Team",
            email = "dev@insuranceapp.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "Local Development"),
        @Server(url = "https://api.insuranceapp.com/user-service", description = "Production")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Bearer token. Obtain via POST /api/auth/login",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Configuration is done via annotations above
}
