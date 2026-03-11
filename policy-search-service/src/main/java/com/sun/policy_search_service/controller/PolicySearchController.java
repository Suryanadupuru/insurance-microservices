package com.sun.policy_search_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sun.policy_search_service.dto.request.PolicySearchRequest;
import com.sun.policy_search_service.dto.response.PolicyProductResponse;
import com.sun.policy_search_service.dto.response.PolicySearchResponse;
import com.sun.policy_search_service.service.PolicySearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * PolicySearchController — all endpoints public .
 *
 * Base path: /policy-search
 */

@RestController
@RequestMapping("/policy-search")
@RequiredArgsConstructor
@Tag(name = "Policy Search API", description = "Endpoints for searching insurance policies")
public class PolicySearchController {

	private final PolicySearchService searchService;

	@GetMapping("/public")
	@Operation(summary = "Search active policies — Guest access", description = """
			Search and filter insurance policies by any combination of:
			keyword, type, coverage range, premium, duration.

			All parameters are optional. Returns a paginated, sorted result.

			Examples:
			  /policy-search/public?type=HEALTH
			  /policy-search/public?keyword=life&maxMonthlyPremium=3000
			  /policy-search/public?minCoverage=500000&sortBy=monthlyPremium&sortDir=asc
			""")
	public ResponseEntity<PolicySearchResponse> pulbicSearch(
			@Valid @ModelAttribute PolicySearchRequest request) {

		PolicySearchResponse response = searchService.search(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	@Operation(
			summary = "Search active policies — Authenticated users", 
			description = "Same as /public but requires a logged-in user. Reserved for personalised features.")
	public ResponseEntity<PolicySearchResponse> authenticatedSerach(
			@Valid @ModelAttribute PolicySearchRequest request) {

		PolicySearchResponse response = searchService.search(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get full details of a single policy product by ID")
	public ResponseEntity<PolicyProductResponse> getById(
			@Parameter(description = "Policy product ID") @PathVariable Long id) {
		return ResponseEntity.ok(searchService.getById(id));
	}

	@GetMapping("/types")
	@Operation(
			summary = "List all policy types",
			description = "Returns the list of all available PolicyType enum values. Used to populate search filter dropdowns.")
	public ResponseEntity<List<String>> getTypes() {
		return ResponseEntity.ok(searchService.getAvailableTypes());
	}

}
