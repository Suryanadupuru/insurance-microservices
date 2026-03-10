package com.sun.policy_service.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

/**
 * UserPolicy — represents a user's enrollment in a PolicyProduct.
 *
 * Created when a user applies for a policy.
 * Managed by the user.
 *
 * Table: user_policies
 */
@Entity
@Table(name = "user_policies", indexes = {
		@Index(name = "idx_user_policy_user_id", columnList = "userId"),
		@Index(name = "idx_user_policy_product_id", columnList = "policyNumber", unique = true)
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPolicy {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	
	@Column(nullable = false)
	private Long userId; // ID of the user who owns this policy and from X-User-Id gateway header
	
	@Column(nullable = false)
	private String userEmail; //from X-Username gateway header (for display)
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "policy_product_id", nullable = false)
	private PolicyProduct policyProduct; // The policy product this enrollment is for (many-to-one relationship)

	@Column(nullable = false, unique = true, length = 30)
	private String policyNumber; // Unique policy number assigned at enrollment (e.g. "POL123456789")
	
	
	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal coverageAmount; // Copied from PolicyProduct at enrollment time (for historical record)
	
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal premiumPaid; // Premium paid by the user for this policy at time of enrollment(updated with payments)
	
	
	@Column(nullable = false)
	private LocalDate startDate; // Policy start date (set at enrollment)
	
	@Column(nullable = false)
	private LocalDate endDate; // Policy end date (calculated from start date + duration)
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private PolicyStatus status = PolicyStatus.PENDING; // Current status of the policy (default to PENDING at enrollment)
	
	
	private String cancellationReason; // Reason for cancellation if status is CANCELLED (optional)
	
	private LocalDateTime cancelledAt; // Timestamp of when the policy was cancelled (optional)
	
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt; // Timestamp of when the policy enrollment was created (set at enrollment)
	
	@UpdateTimestamp
	private LocalDateTime updatedAt; // Timestamp of last update to the policy enrollment (updated on changes)

}
