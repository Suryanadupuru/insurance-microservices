package com.sun.policy_search_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

/**
 * Read-only view of the policy_products table owned by policy-service.
 *
 * This service never writes to this table — it only queries it.
 * ddl-auto=none ensures Hibernate never touches the schema.
 *
 * Table: policy_products (created and managed by policy-service)
 */

@Entity
@Table(name = "policy_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyProduct {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false, length = 150)
	private String name;
	
	@Column(nullable = false, length = 1000)
	private String description;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PolicyType type;
	
	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal coverageAmount;
	
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal monthlyPremium;
	
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal annualPremium;
	
	 @Column(nullable = false)
	 private Integer durationMonths;

	 private Integer minAge;
	 private Integer maxAge;
	 
	 @Column(nullable = false)
	 private boolean active;

	 @Column(updatable = false)
	 private LocalDateTime createdAt;

	 private LocalDateTime updatedAt;
	 
}
