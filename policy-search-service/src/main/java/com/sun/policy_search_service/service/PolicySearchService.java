package com.sun.policy_search_service.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.policy_search_service.repository.PolicySearchRepository;
import com.sun.policy_search_service.dto.request.PolicySearchRequest;
import com.sun.policy_search_service.dto.response.PolicySearchResponse;
import com.sun.policy_search_service.dto.response.PolicyProductResponse;
import com.sun.policy_search_service.entity.PolicyProduct;
import com.sun.policy_search_service.exception.PolicyNotFoundException;
import com.sun.policy_search_service.entity.PolicyType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PolicySearchService: Insurance Policy Search.
 *
 * Read-only. Delegates dynamic filtering to PolicyProductSpecification.
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicySearchService {
	
	private final PolicySearchRepository searchRepository;
	
	 // Allowed sort fields — prevents arbitrary column injection
	private static final Set<String> ALLOWED_SORT_FIELDS = 
			Set.of("name", "monthlyPremium", "coverageAmount", "durationMonths", "annualPremium");
	
	// Search
	
	public PolicySearchResponse search(PolicySearchRequest request) {
		
		Pageable pageable = buildPageable(request);
		
		Page<PolicyProduct> page = searchRepository.findAll(
				PolicyProductSpecification.from(request), pageable);
		
		log.debug("Policy search returned {} results (page={}, size={})",
	                page.getTotalElements(), request.getPage(), request.getSize());
		 
		List<PolicyProductResponse> policies = page.getContent()
	                .stream().map(this::toResponse).toList();
		
		return PolicySearchResponse.builder()
				.policies(policies)
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.currentPage(page.getNumber())
				.pageSize(page.getSize())
				.first(page.isFirst())
				.last(page.isLast())
				.build();
		
	}
	
	// ── Get One
	
	public PolicyProductResponse getById(Long id) {
		PolicyProduct product = searchRepository.findById(id)
				.orElseThrow(() -> new PolicyNotFoundException("Provided id does not exist."+id));
		if(!product.isActive()) {
			throw new PolicyNotFoundException("Policy product is no longer available.");
		}
		return toResponse(product);
	}
	
	// ── Available Types
	
	/**
     * Returns all policy types — used to populate search filter dropdowns.
     */
	
	public List<String> getAvailableTypes() {
		return Arrays.stream(PolicyType.values())
				.map(Enum::name)
				.toList();
	}
	
	// ── Private Helpers
	
	private Pageable buildPageable(PolicySearchRequest request) {
		String sortField = ALLOWED_SORT_FIELDS.contains(request.getSortBy())
                ? request.getSortBy()
                : "name";

        Sort sort = "desc".equalsIgnoreCase(request.getSortDir())
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        return PageRequest.of(request.getPage(), request.getSize(), sort);
	}
	
	
	private PolicyProductResponse toResponse(PolicyProduct p) {
        return PolicyProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .type(p.getType())
                .coverageAmount(p.getCoverageAmount())
                .monthlyPremium(p.getMonthlyPremium())
                .annualPremium(p.getAnnualPremium())
                .durationMonths(p.getDurationMonths())
                .minAge(p.getMinAge())
                .maxAge(p.getMaxAge())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

}
