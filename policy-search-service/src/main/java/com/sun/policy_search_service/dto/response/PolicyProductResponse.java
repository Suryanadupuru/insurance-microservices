package com.sun.policy_search_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.sun.policy_search_service.entity.PolicyType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full details of a single policy product returned in search results.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyProductResponse {

	private Long id;
    private String name;
    private String description;
    private PolicyType type;

    private BigDecimal coverageAmount;
    private BigDecimal monthlyPremium;
    private BigDecimal annualPremium;

    private Integer durationMonths;
    private Integer minAge;
    private Integer maxAge;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
