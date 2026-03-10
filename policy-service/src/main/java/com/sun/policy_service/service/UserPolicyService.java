package com.sun.policy_service.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.policy_service.dto.request.ApplyPolicyRequest;
import com.sun.policy_service.repository.UserPolicyRepository;
import com.sun.policy_service.dto.response.UserPolicyResponse;
import com.sun.policy_service.entity.UserPolicy;
import com.sun.policy_service.exception.PolicyNotActiveException;
import com.sun.policy_service.entity.PolicyProduct;
import com.sun.policy_service.entity.PolicyStatus;
import com.sun.policy_service.exception.PolicyNotFoundException;
import com.sun.policy_service.exception.UnauthorizedPolicyAccessException;
import com.sun.policy_service.dto.request.CancelPolicyRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * UserPolicyService — handles policy applications and lifecycle management.
 */


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserPolicyService {

	private UserPolicyRepository userPolicyRepository;
	private PolicyProductService policyProductService;
	
	
	// ── Apply for a Policy 
	
	
	public UserPolicyResponse apply(Long userId, String userEmail, ApplyPolicyRequest request) {
		// TODO Auto-generated method stub
		PolicyProduct product = policyProductService.findById(request.getPolicyProductId());
		
		if(!product.isActive()) {
			log.warn("Attempt to apply for inactive product: {}", product.getId());
			throw new PolicyNotActiveException("Policy '"+product.getName()+"' is currently not active.");
		}
		
		// Calculate end date based on product duration
		var startDate = request.getStartDate();
		var endDate = startDate.plusMonths(product.getDurationMonths());
		
		UserPolicy userPolicy = UserPolicy.builder()
				.userId(userId)
				.userEmail(userEmail)
				.policyProduct(product)
				.policyNumber(generatePolicyNumber())
				.coverageAmount(product.getCoverageAmount())
				.premiumPaid(product.getMonthlyPremium())
				.startDate(startDate)
				.endDate(endDate)
				.status(PolicyStatus.ACTIVE)
				.build();
		
		UserPolicy savedPolicy = userPolicyRepository.save(userPolicy);
		log.info("Policy application submitted: policyNumber={}, userId={}",
                savedPolicy.getPolicyNumber(), userId);
		
		return toResponse(savedPolicy);
	}
	
	
	// ── Get My Policies
	
	@Transactional(readOnly = true)
	public List<UserPolicyResponse> getMyPolicies(Long userId) {
		return userPolicyRepository.findByUserId(userId)
				.stream().map(this::toResponse).toList();
		
	}
	
	// ── Get One of My Policies
	
	@Transactional(readOnly = true)
	public UserPolicyResponse getMyPolicy(Long userId, Long policyId) {
		UserPolicy policy = findById(policyId);
		verifyOwnership(policy, userId);		
		return toResponse(policy);
	}
	
	
	// ── Cancel a Policy
	
	public UserPolicyResponse cancelPolicy(Long userId, Long policyId, CancelPolicyRequest request) {
		UserPolicy policy = findById(policyId);
		verifyOwnership(policy, userId);
		
		if(policy.getStatus() == PolicyStatus.CANCELLED) {
			log.warn("Attempt to cancel already cancelled policy: policyId={}, status={}", policyId, policy.getStatus());
			throw new PolicyNotActiveException("Only active policies can be cancelled.");
		}
		if(policy.getStatus() == PolicyStatus.EXPIRED) {
			log.warn("Attempt to cancel expired policy: policyId={}, status={}", policyId, policy.getStatus());
			throw new PolicyNotActiveException("Cannot cancel an expired policy.");
		}
		policy.setStatus(PolicyStatus.CANCELLED);
		policy.setCancellationReason(request.getCancellationReason());
		policy.setCancelledAt(LocalDateTime.now());
	
		UserPolicy updatedPolicy = userPolicyRepository.save(policy);
		log.info("Policy cancelled: policyNumber={}, userId={}, reason={}",
				updatedPolicy.getPolicyNumber(), userId, request.getCancellationReason());
		
		return toResponse(updatedPolicy);
	}
	
	
	// ── Admin: Get Any User's Policies
	
	@Transactional(readOnly = true)
	public List<UserPolicyResponse> getPoliciesByUserId(Long userId) {
		return userPolicyRepository.findByUserId(userId)
				.stream().map(this::toResponse).toList();
	}

	// ── Private Helpers
	
	private UserPolicy findById(Long policyId) {
		// TODO Auto-generated method stub
		return userPolicyRepository.findById(policyId)
				.orElseThrow(() -> new PolicyNotFoundException( policyId));
	}
	
	
	private void verifyOwnership(UserPolicy policy, Long userId) {
		// TODO Auto-generated method stub
		throw new UnauthorizedPolicyAccessException(policy.getId(), userId);
	}


	/**
     * Generates a unique policy number: POL-YYYYMM-XXXXXX
     * e.g. POL-202501-000123
     */
	
	private String generatePolicyNumber() {
		String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
		Long count =  userPolicyRepository.count() + 1; // Simple count-based sequence (for demo purposes)
		
		// Simple random policy number generator (for demo purposes)
		return String.format("POL-%s-%06d", datePart, count);
	}
	
	// ── Response Mapping
	
	public UserPolicyResponse toResponse(UserPolicy userPolicy) {
		return UserPolicyResponse.builder()
				.id(userPolicy.getId())
				.policyNumber(userPolicy.getPolicyNumber())
				.userId(userPolicy.getUserId())
				.userEmail(userPolicy.getUserEmail())
				.policyProductId(userPolicy.getPolicyProduct().getId())
				.policyProductName(userPolicy.getPolicyProduct().getName())
				.policyType(userPolicy.getPolicyProduct().getType())
				.coverageAmount(userPolicy.getCoverageAmount())
				.premiumPaid(userPolicy.getPremiumPaid())
				.startDate(userPolicy.getStartDate())
				.endDate(userPolicy.getEndDate())
				.status(userPolicy.getStatus())
				.cancellationReason(userPolicy.getCancellationReason())
				.cancelledAt(userPolicy.getCancelledAt())
				.createdAt(userPolicy.getCreatedAt())
				.updatedAt(userPolicy.getUpdatedAt())
				.build();
	}
	
}
