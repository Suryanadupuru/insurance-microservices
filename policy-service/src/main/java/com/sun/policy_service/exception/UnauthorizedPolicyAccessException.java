package com.sun.policy_service.exception;

public class UnauthorizedPolicyAccessException extends RuntimeException {
	
	public UnauthorizedPolicyAccessException(Long policyId, Long userId) {
		super("User with id "+userId+" is not authorized to access policy with id "+policyId);
	}

}
