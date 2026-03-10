package com.sun.policy_service.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.policy_service.dto.response.PolicyProductResponse;
import com.sun.policy_service.entity.PolicyType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePolicyProductRequest {
	
	private String name;
	private String description;
	private PolicyType type;
	private BigDecimal coverageAmount;
	private BigDecimal monthlyPremium;
	private BigDecimal annualPremium;
	private Integer durationMonths;
	private Integer minAge;
	private Integer maxAge;
	

}
