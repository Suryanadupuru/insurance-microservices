package com.sun.user_service.exception;
// Account Locked — thrown when user exceeds max failed login attempts

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.LOCKED)

public class AccountLockedException extends RuntimeException {
	public AccountLockedException(String message) {
		super(message);
	}
}