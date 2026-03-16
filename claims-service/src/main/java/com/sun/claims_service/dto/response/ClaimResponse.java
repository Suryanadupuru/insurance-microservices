package com.sun.claims_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sun.claims_service.entity.ClaimType;
import com.sun.claims_service.entity.ClaimStatus;
/**
 * Full claim details returned to both users and admins.
 * adminNotes is visible only to admins — filter in controller if needed.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
	
	private Long id;
	private String claimNumber;
	
	// Policy reference
    private Long userPolicyId;
    private String policyNumber;
    
    // Ownership
    private Long userId;
    private String userEmail;
    
    // Incident
    private ClaimType claimType;
    private LocalDate incidentDate;
    private String incidentDescription;
    private String incidentLocation;
    
    // Financials
    private BigDecimal claimAmount;
    private BigDecimal approvedAmount;
    
    // Status & resolution
    private ClaimStatus status;
    private String adminNotes;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
    private LocalDateTime paidAt;
    
    // Audit
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

}
