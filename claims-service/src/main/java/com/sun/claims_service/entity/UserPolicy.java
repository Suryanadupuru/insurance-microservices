package com.sun.claims_service.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Read-only view of the user_policies table owned by policy-service.
 *
 * Claims-service reads this table only to validate:
 *   1. The policy exists
 *   2. The policy belongs to the requesting user (userId matches)
 *   3. The policy status is ACTIVE
 *
 * This service never writes to this table.
 * ddl-auto=none ensures Hibernate never touches the schema.
 */

@Entity
@Table(name = "user_policies")
@Getter
@NoArgsConstructor
public class UserPolicy {
	
	
	@Id
	private Long id;
	
	private Long userId;
	private String userEmail;
	private String policyNumber;
	
	@Column(name = "policy_product_id")
	private Long policyProductId;
	
	private BigDecimal coverageAmount;
	private LocalDate startDate;
	private LocalDate endDate;
	
	// Stored as VARCHAR in DB — compare as String to avoid enum duplication
    private String status;

}
