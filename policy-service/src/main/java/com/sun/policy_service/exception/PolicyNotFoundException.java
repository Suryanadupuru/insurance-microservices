package com.sun.policy_service.exception;

public class PolicyNotFoundException extends RuntimeException {

	public PolicyNotFoundException(Long id) {
		super("Policy not found with id"+id);
	}

}
