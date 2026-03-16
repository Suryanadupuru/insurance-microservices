package com.sun.claims_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sun.claims_service.service.ClaimsService;
import com.sun.claims_service.entity.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.sun.claims_service.dto.request.*;
import com.sun.claims_service.dto.response.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * ClaimsController
 *
 * Base path: /claims
 * Via gateway: /api/claims (StripPrefix=1 → /claims)
 */

@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimsController {
	
	private final ClaimsService claimsService;
	
	// ── USER ENDPOINTS 
	
	@Tag(name = "Claims - User")
	@PostMapping
	@Operation(
			summary     = "Submit a new claim",
	        description = """
	            Submit a claim against one of your active policies.
	            
	            Validations performed:
	            - Policy must exist and belong to you
	            - Policy status must be ACTIVE
	            - Incident date must fall within the policy coverage period
	            - Claim amount must not exceed the policy coverage amount
	            """
			)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ClaimResponse> submitClaim(
			@RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Username") String userEmail,
            @Valid @RequestBody SubmitClaimRequest request){
		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(claimsService.submit(userId, userEmail, request));
	}
	
	@Tag(name = "Claims — User")
    @GetMapping("/my")
    @Operation(summary = "List all my submitted claims")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(
            @RequestHeader("X-User-Id") Long userId) {
 
        return ResponseEntity.ok(claimsService.getMyClaims(userId));
    }
	
	@Tag(name = "Claims — User")
    @GetMapping("/my/{id}")
    @Operation(summary = "Get one of my claims by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClaimResponse> getMyClaim(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
 
        return ResponseEntity.ok(claimsService.getMyClaim(userId, id));
    }
	
	@Tag(name = "Claims — User")
    @GetMapping("/policy/{policyNumber}")
    @Operation(summary = "List my claims for a specific policy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClaimResponse>> getClaimsByPolicy(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String policyNumber) {
 
        return ResponseEntity.ok(claimsService.getClaimsByPolicy(userId, policyNumber));
    }

	// ── ADMIN ENDPOINTS
	
	@Tag(name = "Claims — Admin")
    @GetMapping
    @Operation(summary = "List all claims — Admin only",
               description = "Optionally filter by status query param: ?status=SUBMITTED")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClaimResponse>> getAllClaims(
            @RequestParam(required = false) ClaimStatus status) {
 
        List<ClaimResponse> claims = (status != null)
                ? claimsService.getClaimsByStatus(status)
                : claimsService.getAllClaims();
 
        return ResponseEntity.ok(claims);
    }
	
	@Tag(name = "Claims — Admin")
    @GetMapping("/{id}")
    @Operation(summary = "Get any claim by ID — Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(claimsService.getClaimById(id));
    }
	
	@Tag(name = "Claims — Admin")
    @PutMapping("/{id}/status")
    @Operation(
        summary     = "Update claim status — Admin only",
        description = """
            Allowed status transitions:
              SUBMITTED    → UNDER_REVIEW
              UNDER_REVIEW → APPROVED (requires approvedAmount) | REJECTED (requires rejectionReason)
              APPROVED     → PAID
              REJECTED     → (terminal — no further transitions)
              PAID         → (terminal — no further transitions)
            """
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> updateClaimStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClaimStatusRequest request) {
 
        return ResponseEntity.ok(claimsService.updateStatus(id, request));
    }
}
