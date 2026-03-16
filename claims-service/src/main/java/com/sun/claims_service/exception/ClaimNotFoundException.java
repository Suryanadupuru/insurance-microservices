package com.sun.claims_service.exception;

public class ClaimNotFoundException extends RuntimeException{

	public ClaimNotFoundException(Long claimId) {
		super ("Claim id not found "+ claimId);
	}
	

}
