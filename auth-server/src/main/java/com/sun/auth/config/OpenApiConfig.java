package com.sun.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI available at: http://localhost:8090/swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Insurance Platform — Auth Server API",
        version = "1.0.0",
        description = """
            Authentication and Authorization API.
            
            **Flow:**
            1. Register via `POST /auth/register`
            2. Verify email via link sent to inbox
            3. Login via `POST /auth/login` → receive accessToken + refreshToken
            4. Pass `Authorization: Bearer <accessToken>` on all service calls
            5. Refresh silently via `POST /auth/refresh` before expiry (15 min)
            """,
        contact = @Contact(name = "Insurance Platform Team", email = "dev@insuranceapp.com")
    ),
    servers = {
        @Server(url = "http://localhost:8090", description = "Local Development")
    }
)
public class OpenApiConfig {}