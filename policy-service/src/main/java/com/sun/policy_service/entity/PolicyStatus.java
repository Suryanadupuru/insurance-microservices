package com.sun.policy_service.entity;

/**
 * LifeCycle Status of a UserPolicy enrollment.
 * 
 */
public enum PolicyStatus {
	
	PENDING,    // User has applied but payment is not completed yet
	ACTIVE,     // Payment completed and policy is active
	SUSPENDED,   // Policy is temporarily suspended (e.g. due to non-payment)
	CANCELLED,   // Policy is cancelled by user or admin
	EXPIRED      // Policy term has ended

}
