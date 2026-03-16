package com.sun.claims_service.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Claim — a user's request for compensation under an active policy.
 *
 * Table: claims (owned by this service)
 */

@Entity
@Table(name = "claims", indexes = {
		@Index(name = "idx_claim_user_id", columnList = "userId"),
		@Index(name = "idx_claim_policy_number", columnList = "policyNumber"),
		@Index(name = "idx_claim_number", columnList = "claimNumber"),
		@Index(name = "idx_claim_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ── Ownership
	
	@Column(nullable = false)
	private Long userId; // ID of the user who filed the claim
	
	@Column(nullable = false, length = 150)
	private String userEmail; // Email of the user who filed the claim (for quick reference)
	
	// ── Policy Reference 
	
	@Column(nullable = false)
	private Long userPolicyId; // ID of the associated policy (for internal reference)
	
	@Column(nullable = false)
	private String policyNumber; // Associated insurance policy number
	
	// ── Claim Identity 
	
	@Column(nullable = false, unique = true, length = 50)
	private String claimNumber; // Unique identifier for the claim

	// ── Incident Details 
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ClaimType claimType; // Type of claim (e.g., "ACCIDENT", "THEFT", "FIRE", etc.)

	@Column(nullable = false)
	private LocalDate incidentDate; // Date when the incident occurred
	
	@Column(nullable = false)
	private String incidentDescription; // Description of the claim

	@Column(nullable = false)
	private String incidentLocation; // Location where the incident occurred
	
	// ── Financials 
	
	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal claimAmount; // Amount claimed by the user
	
	@Column(precision = 15, scale = 2)
	private BigDecimal approvedAmount; // Amount approved by the insurance company (can be null until processed)

	// ── Status & Resolution
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ClaimStatus status = ClaimStatus.SUBMITTED; // Current status of the claim (e.g., "PENDING", "APPROVED", "REJECTED", "IN_REVIEW")
	
	@Column(length = 1000)
	private String adminNotes; // Notes from the claims adjuster or admin handling the claim
	
	@Column(length = 1000)
	private String rejectionReason; // Reason for rejection if the claim is rejected (can be null)
	
	private LocalDateTime reviewedAt; // Timestamp when the claim was reviewed by an adjuster (can be null until processed)
	private LocalDateTime paidAt; // Timestamp when the claim was paid out (can be null until processed)
	
	// ── Audit 
	
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime submittedAt; // Timestamp when the claim was submitted
	
	@UpdateTimestamp
	private LocalDateTime updatedAt; // Timestamp when the claim was last updated (can be null until processed)
	
	
	
}
