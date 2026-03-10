package com.sun.policy_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PolicyProduct — an insurance product in the catalog.
 *
 * Managed by admins. Users browse these and apply for them.
 * One product can have many UserPolicy enrollments.
 *
 * Table: policy_products
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
    private Long id;

    // ── Product Details ───────────────────────────────────────────────────────

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyType type;

    // ── Coverage and Pricing ──────────────────────────────────────────────────

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal coverageAmount;      // max coverage e.g. 500000.00

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPremium;      // monthly cost to the user

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal annualPremium;       // annual cost (usually < 12x monthly)

    @Column(nullable = false)
    private Integer durationMonths;         // policy term e.g. 12, 24, 36

    // ── Eligibility ───────────────────────────────────────────────────────────

    private Integer minAge;
    private Integer maxAge;

    // ── Status ────────────────────────────────────────────────────────────────

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;          // false = deactivated by admin

    // ── Audit ─────────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}