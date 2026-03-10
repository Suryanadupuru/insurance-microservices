package com.sun.policy_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.policy_service.repository.PolicyProductRepository;
import com.sun.policy_service.dto.request.CreatePolicyProductRequest;
import com.sun.policy_service.dto.request.UpdatePolicyProductRequest;
import com.sun.policy_service.dto.response.PolicyProductResponse;
import com.sun.policy_service.entity.PolicyProduct;
import com.sun.policy_service.entity.PolicyType;
import com.sun.policy_service.exception.PolicyNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PolicyProductService — manages the insurance product catalog.
 * Admin-only write operations. Public read operations.
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PolicyProductService {
	
	
	private final PolicyProductRepository policyProductRepository;
	
	public PolicyProductResponse create(CreatePolicyProductRequest request) {
		
		// Validate request (e.g., check for duplicate product name)
		if (policyProductRepository.existsByName(request.getName())) {
			throw new IllegalArgumentException("Policy product with this name already exists");
		}
		
		// Create and save new policy product
		PolicyProduct product = PolicyProduct.builder()
				.name(request.getName())
				.description(request.getDescription())
				.type(request.getType())
				.coverageAmount(request.getCoverageAmount())
				.monthlyPremium(request.getMonthlyPremium())
				.annualPremium(request.getAnnualPremium())
				.durationMonths(request.getDurationMonths())
				.minAge(request.getMinAge())
				.maxAge(request.getMaxAge())
				.active(true) // New products are active by default
				.build();
		
		PolicyProduct saved = policyProductRepository.save(product);
		log.info("Created new policy product: {}", saved.getName(),saved.getId());
		return toResponse(saved);
	}
	
	@Transactional(readOnly = true)
	public List<PolicyProductResponse> getAllActive() {
		return policyProductRepository.findByActiveTrue().stream()
				.map(this::toResponse).toList();
	}
	
	@Transactional(readOnly = true)
	public List<PolicyProductResponse> getActiveByType(PolicyType type) {
		return policyProductRepository.findByActiveTrueAndType(type).stream()
				.map(this::toResponse).toList();
	}
	
	public PolicyProductResponse getById(Long id) {
		return toResponse(findById(id));
	}
	
	public PolicyProductResponse update(Long id, UpdatePolicyProductRequest request) {
		PolicyProduct product = findById(id);
		if(request.getName()           != null) product.setName(request.getName());
		if(request.getDescription()    != null) product.setDescription(request.getDescription());
		if(request.getType()           != null) product.setType(request.getType());
		if(request.getCoverageAmount() != null) product.setCoverageAmount(request.getCoverageAmount());
		if(request.getMonthlyPremium() != null) product.setMonthlyPremium(request.getMonthlyPremium());
		if(request.getAnnualPremium()  != null) product.setAnnualPremium(request.getAnnualPremium());
		if(request.getDurationMonths() != null) product.setDurationMonths(request.getDurationMonths());
		if(request.getMinAge()         != null) product.setMinAge(request.getMinAge());
		if(request.getMaxAge()         != null) product.setMaxAge(request.getMaxAge());
		if(request.getActive()         != null) product.setActive(request.getActive());
		
		PolicyProduct updated = policyProductRepository.save(product);
		log.info("Updated policy product: {}",id);
		return toResponse(updated);
	}
	
	// Deactivate (Admin) — soft delete
	
	public void deactivate(Long id) {
		PolicyProduct product = findById(id);
		product.setActive(false);
		policyProductRepository.save(product);
		log.info("Deactivated policy product: {}",id);
	}
	
	
	// Internal helper (used by UserPolicyService)
	
	public PolicyProduct findById(Long id) {
        return policyProductRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
    }
	
	// Response Mapping
	
	public PolicyProductResponse toResponse(PolicyProduct product) {
		return PolicyProductResponse.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.type(product.getType())
				.coverageAmount(product.getCoverageAmount())
				.monthlyPremium(product.getMonthlyPremium())
				.annualPremium(product.getAnnualPremium())
				.durationMonths(product.getDurationMonths())
				.minAge(product.getMinAge())
				.maxAge(product.getMaxAge())
				.active(product.isActive())
				.createdAt(product.getCreatedAt())
				.updatedAt(product.getUpdatedAt())
				.build();
	}
	

}