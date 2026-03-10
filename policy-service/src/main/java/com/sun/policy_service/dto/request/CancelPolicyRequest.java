package com.sun.policy_service.dto.request;

import java.math.BigDecimal;

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
public class CancelPolicyRequest {

	private String cancellationReason;

}
