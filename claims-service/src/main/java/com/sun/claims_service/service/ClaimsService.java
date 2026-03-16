package com.sun.claims_service.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sun.claims_service.repository.ClaimRepository;
import com.sun.claims_service.repository.UserPolicyRepository;
import com.sun.claims_service.dto.request.SubmitClaimRequest;
import com.sun.claims_service.dto.request.UpdateClaimStatusRequest;
import com.sun.claims_service.dto.response.ClaimResponse;
import com.sun.claims_service.entity.Claim;
import com.sun.claims_service.entity.ClaimStatus;
import com.sun.claims_service.entity.UserPolicy;
import com.sun.claims_service.exception.ClaimNotFoundException;
import com.sun.claims_service.exception.InvalidClaimStatusTransitionException;
import com.sun.claims_service.exception.PolicyValidationException;
import com.sun.claims_service.exception.UnauthorizedClaimAccessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ClaimsService — Claims Submission.
 *
 * Submit flow:
 *   1. Look up policy by policyNumber + userId   → must exist and belong to user
 *   2. Verify policy status is ACTIVE             → reject PENDING/EXPIRED/CANCELLED
 *   3. Verify incidentDate is within policy dates → can't claim outside coverage period
 *   4. Verify claimAmount <= coverageAmount        → can't claim more than coverage
 *   5. Generate unique claimNumber and persist
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClaimsService {
	
	private final ClaimRepository claimRepository;
	private final UserPolicyRepository userPolicyRepository;
	
	// ── Submit Claim
	
	public ClaimResponse submit(Long userId, String userEmail, SubmitClaimRequest request) {
		
		// Step 1: Validate policy belongs to this user
		UserPolicy policy = userPolicyRepository
				.findByPolicyNumberAndUserId(request.getPolicyNumber(), userId)
				.orElseThrow(() -> new PolicyValidationException(
						"Policy '" + request.getPolicyNumber() + "' not found or does not belong to you."));
		
		// Step 2: Policy must be ACTIVE
		if(!"ACTIVE".equals(policy.getStatus())) {
			throw new PolicyValidationException(
					"Claims can only be submitted against ACTIVE policies. " +
		            "Policy '" + request.getPolicyNumber() + "' is currently " + policy.getStatus() + ".");
		}
		
		// Step 3: Incident date must be within the policy coverage period
		if(request.getIncidentDate().isBefore(policy.getStartDate()) || 
				request.getIncidentDate().isAfter(policy.getEndDate())) {
			throw new PolicyValidationException(
					"Incident data " + request.getIncidentDate() + "is outside the policy coverage period(" +
							policy.getStartDate() + " to " + policy.getEndDate() + ").");
		}
		
		//Step 4: Claim amount must not exceed coverage
		if(request.getClaimAmount().compareTo(policy.getCoverageAmount()) > 0) {
			throw new PolicyValidationException(
					"Claim amount " + request.getClaimAmount() + 
					" exceeds the policy coverage amount of " + policy.getCoverageAmount() + ".");
		}
		
		//Step 5: Build and persist the claim
		Claim claim = Claim.builder()
				.userId(userId)
				.userEmail(userEmail)
				.userPolicyId(policy.getId())
				.policyNumber(policy.getPolicyNumber())
				.claimNumber(generateClaimNumber())
				.claimType(request.getClaimType())
				.incidentDate(request.getIncidentDate())
				.incidentDescription(request.getIncidentDescription())
				.incidentLocation(request.getIncidentLocation())
				.claimAmount(request.getClaimAmount())
				.status(ClaimStatus.SUBMITTED)
				.build();
		
		Claim saved =  claimRepository.save(claim);
		log.info("Claim submitted: claimNumber={}, userId={}, policy={}",
                saved.getClaimNumber(), userId, saved.getPolicyNumber());
		
		return toResponse(saved);
	}
	
	// ── User: Read Own Claims
	

	@Transactional(readOnly = true)
	public List<ClaimResponse> getMyClaims(Long userId){
		return claimRepository.findByUserIdOrderBySubmittedAtDesc(userId)
				.stream().map(this::toResponse).toList();
	}
	
	@Transactional(readOnly = true)
	public ClaimResponse getMyClaim(Long userId,  Long claimId) {
		Claim claim = findById(claimId);
		if (!claim.getUserId().equals(userId)) {
			throw new UnauthorizedClaimAccessException();
		}
		return toResponse(claim);
	}
	
	
	public List<ClaimResponse> getClaimsByPolicy(Long userId, String policyNumber){
		return claimRepository
				.findByUserIdAndPolicyNumberOrderBySubmittedAtDesc(userId, policyNumber)
				.stream().map(this::toResponse).toList();
	}
	
	
	// ── Admin: Read All Claims
	
	@Transactional(readOnly = true)
	public List<ClaimResponse> getAllClaims(){
		return claimRepository.findAll()
				.stream().map(this::toResponse).toList();
	}
	
	@Transactional(readOnly = true)
	public ClaimResponse getClaimById(Long claimId) {
		return toResponse(findById(claimId));
	}
	
	@Transactional(readOnly = true)
	public List<ClaimResponse> getClaimsByStatus(ClaimStatus status){
		return claimRepository.findByStatusOrderBySubmittedAtDesc(status)
				.stream().map(this::toResponse).toList();
	}
	
	
	// ── Admin: Update Status
	
	public ClaimResponse updateStatus(Long claimId, UpdateClaimStatusRequest request) {
		
		Claim claim = findById(claimId);
		
		validateStatusTransition(claim.getStatus(), request.getStatus());
		
		// Enforce required fields per target status
		if(request.getStatus() == ClaimStatus.APPROVED) {
			if(request.getApprovedAmount() == null) {
				throw new InvalidClaimStatusTransitionException(
						"Approved amount is required when approving a claim");
			}
			claim.setApprovedAmount(request.getApprovedAmount());
		}
		
		if(request.getStatus() == ClaimStatus.REJECTED) {
			if(request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
				throw new InvalidClaimStatusTransitionException(
						"rejectionReason is required when rejecting a claim.");
			}
			claim.setRejectionReason(request.getRejectionReason());
		}
		
		if (request.getStatus() == ClaimStatus.PAID) {
			claim.setPaidAt(LocalDateTime.now());
		}
		
		claim.setStatus(request.getStatus());
		claim.setAdminNotes(request.getAdminNotes());
		claim.setReviewedAt(LocalDateTime.now());
		
		
		Claim updated = claimRepository.save(claim);
		log.info("Claim status updated: claimNumber={}, newStatus={}",
                updated.getClaimNumber(), updated.getStatus());
		
		return toResponse(updated);
	}
	
	
	// ── Private Helpers
	
	private Claim findById(Long claimId) {
		return claimRepository.findById(claimId)
				.orElseThrow(() -> new ClaimNotFoundException(claimId));
	}
	
	/**
     * Enforces allowed status transitions:
     *   SUBMITTED    → UNDER_REVIEW
     *   UNDER_REVIEW → APPROVED | REJECTED
     *   APPROVED     → PAID
     *   REJECTED     → (terminal)
     *   PAID         → (terminal)
     */
	
	private void validateStatusTransition(ClaimStatus current, ClaimStatus next) {
		
		boolean valid = switch(current) {
			case SUBMITTED    -> next == ClaimStatus.UNDER_REVIEW;
			case UNDER_REVIEW -> next == ClaimStatus.APPROVED || next == ClaimStatus.REJECTED;
			case APPROVED     -> next == ClaimStatus.PAID;
			case REJECTED, PAID -> false;
		};
		
		if(!valid) {
			throw new InvalidClaimStatusTransitionException(
                    "Cannot transition claim from " + current + " to " + next + ".");
		}
	}
	
	
	/**
     * Generates a unique claim number: CLM-YYYYMM-XXXXXX
     * e.g. CLM-202501-000042
     */
	private String generateClaimNumber() {
		String yearMonth = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyyMM"));
		String prefix = "CLM-" + yearMonth + "-";
		long count = claimRepository.countByClaimNumberStartingWith(prefix) + 1;
		
		return prefix + String.format("%06d", count);
	}
	
	// ── Response Mapping
	
	public ClaimResponse toResponse(Claim c) {
		return ClaimResponse.builder()
				.id(c.getId())
				.claimNumber(c.getClaimNumber())
				.userPolicyId(c.getUserPolicyId())
				.policyNumber(c.getPolicyNumber())
				.userId(c.getUserId())
				.userEmail(c.getUserEmail())
				.claimType(c.getClaimType())
				.incidentDate(c.getIncidentDate())
				.incidentDescription(c.getIncidentDescription())
				.incidentLocation(c.getIncidentLocation())
				.claimAmount(c.getClaimAmount())
				.approvedAmount(c.getApprovedAmount())
				.status(c.getStatus())
				.adminNotes(c.getAdminNotes())
				.rejectionReason(c.getRejectionReason())
				.reviewedAt(c.getReviewedAt())
				.paidAt(c.getPaidAt())
				.submittedAt(c.getSubmittedAt())
				.updatedAt(c.getUpdatedAt())
				.build();
				
	}

}
