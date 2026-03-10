package com.sun.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Structured error response returned by GlobalExceptionHandler
 * for all 4xx and 5xx responses.
 *
 * Example validation error:
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "One or more fields have validation errors",
 *   "fieldErrors": {
 *     "email": "Please provide a valid email address",
 *     "password": "Password must be at least 8 characters long"
 *   },
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 *
 * Satisfies BRD NFR 5.6 (error handling) and NFR 5.8 (user-friendly error messages).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** HTTP status code e.g. 400, 401, 404, 409 */
    private int status;

    /** HTTP status reason phrase e.g. "Bad Request", "Not Found" */
    private String error;

    /** Human-readable error message */
    private String message;

    /**
     * Field-level validation errors.
     * Only present when status = 400 (MethodArgumentNotValidException).
     * Key = field name, Value = validation message.
     */
    private Map<String, String> fieldErrors;

    private LocalDateTime timestamp;
}
