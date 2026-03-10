package com.sun.auth.exception;
// Account Not Verified — thrown when user tries to login without verifying email

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountNotVerifiedException extends RuntimeException {
	public AccountNotVerifiedException() {
		super("Account email is not verified. Please check your inbox.");
	}
}