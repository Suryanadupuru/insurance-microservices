package com.sun.claims_service.exception;

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
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex){
		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String field = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			fieldErrors.put(field, message);
		});
		return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
		
	}
	
	@ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ClaimNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }
	
	 @ExceptionHandler(PolicyValidationException.class)
	 public ResponseEntity<Map<String, Object>> handlePolicyValidation(PolicyValidationException ex) {
	     return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
	 }
	 
	 @ExceptionHandler(UnauthorizedClaimAccessException.class)
	 public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedClaimAccessException ex) {
	     return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
	 }
	 
	 @ExceptionHandler(InvalidClaimStatusTransitionException.class)
	 public ResponseEntity<Map<String, Object>> handleInvalidTransition(InvalidClaimStatusTransitionException ex) {
	     return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	 }
	 
	 @ExceptionHandler(Exception.class)
	 public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
	     log.error("Unexpected error in claims-service", ex);
	     return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", null);
	 }
	
	
	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Object details){
		Map<String, Object> body = new HashMap();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		
		if(details != null) {
			body.put("details", details);
		}
		return ResponseEntity.status(status).body(body);
	}

}
