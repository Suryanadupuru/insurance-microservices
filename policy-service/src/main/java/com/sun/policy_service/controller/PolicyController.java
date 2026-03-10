package com.sun.policy_service.controller;

import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.sun.policy_service.service.PolicyProductService;
import com.sun.policy_service.service.UserPolicyService;
import com.sun.policy_service.entity.PolicyType;
import com.sun.policy_service.dto.request.UpdatePolicyProductRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sun.policy_service.dto.request.ApplyPolicyRequest;
import com.sun.policy_service.dto.request.CancelPolicyRequest;
import com.sun.policy_service.dto.request.CreatePolicyProductRequest;

import jakarta.validation.Valid;

import com.sun.policy_service.dto.response.MessageResponse;
import com.sun.policy_service.dto.response.PolicyProductResponse;
import com.sun.policy_service.dto.response.UserPolicyResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyController {
	
	private final PolicyProductService productService;
	private final UserPolicyService userPolicyService;
	
	// ── POLICY CATALOG ────────────────────────────────────────────────────────────────
	
	@Tag(name = "Policy Catalog")
	@PostMapping("/catalog")
	@Operation(summary = "Create a new insurance product", description = "Admin-only endpoint to add a new insurance product to the catalog.")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PolicyProductResponse> createProduct( @Valid @RequestBody CreatePolicyProductRequest request) {
		 return ResponseEntity.status(HttpStatus.CREATED)
	                .body(productService.create(request));
		
	}
	
	@Tag(name = "Policy Catalog")
	@GetMapping("/catalog")
	@Operation(summary = "List insurance products", description = "Public endpoint to list active insurance products. Optionally filter by policy type.")
	public ResponseEntity<List<PolicyProductResponse>> listProducts(@RequestParam(required = false) PolicyType type) {
		List<PolicyProductResponse> products = (type != null)
				? productService.getActiveByType(type)
				: productService.getAllActive();
	
		return ResponseEntity.ok(products);	
	}
	
	@Tag(name = "Policy Catalog")
	@GetMapping("/catalog/{id}")
	@Operation(summary = "Get insurance product details", description = "Public endpoint to get details of a specific insurance product by ID.")
	public ResponseEntity<PolicyProductResponse> getProduct(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getById(id));
	}
	
	
	@Tag(name = "Policy Catalog")
	@PutMapping("/catalog/{id}")
	@Operation(summary = "Update an insurance product", description = "Admin-only endpoint to update details of an existing insurance product.")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PolicyProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdatePolicyProductRequest request) {
		return ResponseEntity.ok(productService.update(id, request));
	}
	
	
	@Tag(name = "Policy Catalog")
	@DeleteMapping("/catalog/{id}")
	@Operation(summary = "Deactivate an insurance product", description = "Admin-only endpoint to deactivate an insurance product. Deactivated products will not be available for purchase.")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<MessageResponse> deactivateProduct(@PathVariable Long id) {
		productService.deactivate(id);
		return ResponseEntity.ok(MessageResponse.of("Policy product deactivated successfully"));
	}
	
	// ── USER POLICIES
	
	@Tag(name = "User Policies")
	@PostMapping("/apply")
	@Operation(summary = "Apply for an insurance policy", description = "Endpoint for users to apply for an insurance policy. Requires user authentication.")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserPolicyResponse> applyForPolicy(
			@RequestHeader("X-User-Id") Long userId,
			@RequestHeader("X-User-Email") String userEmail,
			@Valid @RequestBody ApplyPolicyRequest request){
		
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(userPolicyService.apply(userId, userEmail, request));
	}
	
	
	@Tag(name = "User Policies")
	@GetMapping("/my")
	@Operation(summary = "Get my enrolled insurance policies", description = "Endpoint for users to retrieve their active insurance policies. Requires user authentication.")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<UserPolicyResponse>> getMyPolicies(
			@RequestHeader("X-User-Id") Long userId){
		
		return ResponseEntity.ok(userPolicyService.getMyPolicies(userId));
	}
	
	@Tag(name = "User Policies")
	@GetMapping("/my/{policyId}")
	@Operation(summary = "Get details of my insurance policy", description = "Endpoint for users to retrieve details of a specific enrolled insurance policy. Requires user authentication.")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserPolicyResponse> getMyPolicy(
			@RequestHeader("X-User-Id") Long userId,
			@PathVariable Long policyId){
		
		return ResponseEntity.ok(userPolicyService.getMyPolicy(userId, policyId));
	}
	
	@Tag(name = "User Policies")
	@PutMapping("/my/{policyId}/cancel")
	@Operation(summary = "Cancel an insurance policy", description = "Endpoint for users to cancel an active insurance policy. Requires user authentication.")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserPolicyResponse> cancelPolicy(
			@RequestHeader("X-User-Id") Long userId,
			@PathVariable Long policyId,
			@RequestBody CancelPolicyRequest request){
		
		return ResponseEntity.ok(userPolicyService.cancelPolicy(userId, policyId, request));
		
	}
	
	@Tag(name = "User Policies")
	@GetMapping("/user/{userId}")
	@Operation(summary = "Get insurance policies for a user", description = "Admin-only endpoint to retrieve all insurance policies associated with a specific user.")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserPolicyResponse>> getUserPolicies(
			@PathVariable Long userId){
		
		return ResponseEntity.ok(userPolicyService.getPoliciesByUserId(userId));
	}

}
