package com.sun.policy_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.policy_service.entity.PolicyType;
import com.sun.policy_service.entity.PolicyStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPolicyResponse {
	
	private Long id;
	private String policyNumber;
	private Long userId;
	private String userEmail;
	private Long policyProductId;
	private String policyProductName;
	private PolicyType policyType;
	private BigDecimal coverageAmount;
	private BigDecimal premiumPaid;
	private LocalDate startDate;
	private LocalDate endDate;
	private PolicyStatus status;
	private String cancellationReason;
	private LocalDateTime cancelledAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
