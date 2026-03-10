package com.sun.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response envelope used across all endpoints.
 *
 * Every response — success or failure — is wrapped in this class
 * to ensure a consistent JSON structure for API consumers.
 *
 * Example success:
 * {
 *   "success": true,
 *   "message": "Registration successful!",
 *   "data": { ... },
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 *
 * Example failure:
 * {
 *   "success": false,
 *   "message": "User already exists with email: test@example.com",
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 *
 * @param <T> the type of the data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // ── Static Factory Methods ────────────────────────────────────────────────

    /**
     * Build a successful response with data payload.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Build a successful response with no data payload.
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    /**
     * Build a failure response.
     */
    public static <T> ApiResponse<T> failure(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
