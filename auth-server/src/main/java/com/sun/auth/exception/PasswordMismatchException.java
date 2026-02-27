package com.sun.auth.exception;
// Password Mismatch — thrown when confirm password doesn't match

public class PasswordMismatchException extends RuntimeException {
	public PasswordMismatchException() {
		super("Password and confirm password do not match.");
	}
}
