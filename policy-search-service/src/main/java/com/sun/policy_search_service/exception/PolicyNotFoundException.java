package com.sun.policy_search_service.exception;

public class PolicyNotFoundException extends RuntimeException {
	
	public PolicyNotFoundException(String message) {
		super(message);
	}
	
	public PolicyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
