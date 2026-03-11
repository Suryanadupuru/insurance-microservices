package com.sun.policy_search_service.dto.request;

import java.math.BigDecimal;
import com.sun.policy_search_service.entity.PolicyType;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * All fields are optional and combinable.
 * Passed as @ModelAttribute (request params) — not a @RequestBody.
 *
 * Example:
 *   GET /policy-search/public?type=HEALTH&maxMonthlyPremium=2000&page=0&size=5
 */

@Data
public class PolicySearchRequest {

	// ── Filter Criteria ───────────────────────────────────────────────────────

    /** Free-text search matched against name and description (case-insensitive). */
    private String keyword;

    /** Filter by exact policy type. */
    private PolicyType type;

    /** Minimum coverage amount. */
    private BigDecimal minCoverage;

    /** Maximum coverage amount. */
    private BigDecimal maxCoverage;

    /** Maximum monthly premium — helps users filter by affordability. */
    private BigDecimal maxMonthlyPremium;

    /** Exact duration in months (e.g. 12 for a 1-year policy). */
    private Integer durationMonths;

    // ── Pagination ────────────────────────────────────────────────────────────

    @Min(value = 0, message = "Page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "Size must be >= 1")
    private int size = 10;

    // ── Sorting ───────────────────────────────────────────────────────────────

    /**
     * Field to sort by.
     * Allowed values: name | monthlyPremium | coverageAmount | durationMonths
     * Default: name
     */
    private String sortBy = "name";

    /**
     * Sort direction: asc | desc
     * Default: asc
     */
    private String sortDir = "asc";
}
