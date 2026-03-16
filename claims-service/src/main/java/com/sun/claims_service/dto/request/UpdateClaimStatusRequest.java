package com.sun.claims_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

import com.sun.claims_service.entity.ClaimStatus;

/**
 * Request body for PUT /claims/{id}/status — admin updates a claim's status.
 */

@Data
public class UpdateClaimStatusRequest {

	@NotNull(message = "Status is required")
	private ClaimStatus status;

	/** Required when status = APPROVED. */
    private BigDecimal approvedAmount;
    
    /** Required when status = REJECTED. */
    private String rejectionReason;
    
    /** Optional internal notes from the claim assessor. */
    private String adminNotes;
}
