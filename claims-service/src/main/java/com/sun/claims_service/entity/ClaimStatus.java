package com.sun.claims_service.entity;

/**
 * Full lifecycle of a claim from submission to settlement.
 * Status transitions are admin-driven (PUT /claims/{id}/status).
 * claims-tracking-service reads this to display progress to the user.
 */


public enum ClaimStatus {
	
	SUBMITTED,      // Initial state after user submits
    UNDER_REVIEW,   // Assigned to an assessor
    APPROVED,       // Approved — pending payment
    REJECTED,       // Rejected — reason stored in rejectionReason field
    PAID          // Settlement paid to the user
}
