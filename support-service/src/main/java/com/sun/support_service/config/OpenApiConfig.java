package com.sun.support_service.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Swagger UI: http://localhost:8086/swagger-ui.html
 */

@Configuration
@OpenAPIDefinition(
	    info = @Info(
	        title       = "Insurance Platform — Support Service API",
	        version     = "1.0.0",
	        description = """
	            Customer support ticket management.
	            
	            Users submit support requests which are triaged and resolved by admins.
	            
	            Ticket lifecycle:
	              OPEN → IN_PROGRESS → RESOLVED → CLOSED
	            
	            Users can optionally reference a policy or claim number so the
	            support agent can locate relevant context immediately.
	            """,
	        contact = @Contact(name = "Insurance Platform Team", email = "dev@insuranceapp.com")
	    ),
	    servers = {
	        @Server(url = "http://localhost:8086", description = "Local Development"),
	        @Server(url = "http://localhost:8080/api/support", description = "Via API Gateway")
	    }
	)
public class OpenApiConfig {

}
