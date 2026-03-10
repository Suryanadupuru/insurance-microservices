package com.sun.policy_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.sun.policy_service.entity.PolicyType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
	private boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
