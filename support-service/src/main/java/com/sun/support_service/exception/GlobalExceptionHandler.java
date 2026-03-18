package com.sun.support_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String field = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			fieldErrors.put(field, message);
		});
		return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
	}

	@ExceptionHandler(TicketNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(TicketNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(UnauthorizedTicketAccessException.class)
	public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedTicketAccessException ex) {
		return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
	}
	
	
	@ExceptionHandler(InvalidTicketStatusTransitionException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidTransition(InvalidTicketStatusTransitionException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}
	
	@ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error in support-service", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", null);
    }

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message,
			Object details) {
		Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("message",   message);
        if (details != null) body.put("details", details);
        return ResponseEntity.status(status).body(body);
	}
}
