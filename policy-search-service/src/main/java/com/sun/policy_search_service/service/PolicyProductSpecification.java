package com.sun.policy_search_service.service;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.sun.policy_search_service.dto.request.PolicySearchRequest;
import com.sun.policy_search_service.entity.PolicyProduct;
import com.sun.policy_search_service.entity.PolicyType;
import jakarta.persistence.criteria.Predicate;



/**
 * PolicyProductSpecification — builds dynamic JPA Criteria API predicates
 * from a PolicySearchRequest.
 *
 * Each filter is applied only if the corresponding request field is non-null.
 * All active predicates are AND-ed together.
 *
 * This avoids writing a separate query method for every filter combination —
 * any combination of 0..N filters works with a single repository call.
 */


public class PolicyProductSpecification {
	
	private PolicyProductSpecification() {}
	
	public static Specification<PolicyProduct> from(PolicySearchRequest request) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			
			// Always filter for active policies only — inactive are admin-deactivated
            predicates.add(cb.isTrue(root.get("active")));
            
            // keyword — case-insensitive match on name OR description
            String keyword = request.getKeyword();
			if (keyword != null && !keyword.isBlank()) {
				String pattern = "%" + keyword.toLowerCase() + "%";
				predicates.add(cb.or(
					cb.like(cb.lower(root.get("name")), pattern),
					cb.like(cb.lower(root.get("description")), pattern)
				));
			}
			
			// type — exact enum match
			PolicyType type = request.getType();
			if (type != null) {
				predicates.add(cb.equal(root.get("type"), type));
			}
			
			// minCoverage — coverageAmount >= minCoverage
			BigDecimal minCoverage = request.getMinCoverage();
			if (minCoverage != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("coverageAmount"), minCoverage));
			}
			
			// maxCoverage — coverageAmount <= maxCoverage
			BigDecimal maxCoverage = request.getMaxCoverage();
			if (maxCoverage != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("coverageAmount"), maxCoverage));
			}
			
			// maxMonthlyPremium — monthlyPremium <= maxMonthlyPremium
			BigDecimal maxPremium = request.getMaxMonthlyPremium();
			if (maxPremium != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("monthlyPremium"), maxPremium));
			}
			
			 // durationMonths — exact match
			Integer duration = request.getDurationMonths();
			if (duration != null) {
				predicates.add(cb.equal(root.get("durationMonths"), duration));
			}
			
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

}
