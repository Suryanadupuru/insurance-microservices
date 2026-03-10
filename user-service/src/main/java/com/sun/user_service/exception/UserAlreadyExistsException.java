package com.sun.user_service.exception;
// User Already Exists — thrown when registering a duplicate email

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {
	public UserAlreadyExistsException(String email) {
		super("User already exists with email: " + email);
	}
}