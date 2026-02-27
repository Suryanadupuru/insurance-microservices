package com.sun.auth.entity;

public enum AccountStatus {
	
	PENDING_VERIFICATION,   // Initial state after registration
	ACTIVE,                 // After email verification
	SUSPENDED,              // Manual suspension by admin
	DELETED                 // Soft-deleted (not removed from DB)

}
