package com.sun.user_service.exception;
// User Not Found — thrown when looking up a user that doesn't exist

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(String message) {
		super(message);
	}

	public UserNotFoundException(Long id) {
		super("User not found with id: " + id);
	}
}
