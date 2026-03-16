package com.sun.claims_service.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sun.claims_service.entity.ClaimType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for POST /claims — submit a new claim.
 */

@Data
public class SubmitClaimRequest {
	
	@NotBlank(message = "Policy number is required")
	private String policyNumber;
	
	@NotBlank(message = "Claim type is requried")
	private ClaimType claimType;
	
	@NotBlank(message = "Incident is required")
	@PastOrPresent(message = "Incident date connat be future")
	private LocalDate incidentDate;
	
	@NotBlank(message = "Incident desctiption is required")
	@Size(min = 20, max = 2000,
				message = "Description is required 20 to 2000 characters")
	private String incidentDescription;
	
	@Size( max = 500, message = "Location must not exceed 500 characters")
	private String incidentLocation;
	
	@NotNull(message = "Claim amount is required")
	@DecimalMin(value = "1.00", message = "Claim amount must be at least 1.00")
	private BigDecimal claimAmount;

}
