package com.sun.claims_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sun.claims_service.entity.Claim;
import com.sun.claims_service.entity.ClaimStatus;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long>{
	
	// User's own claims
    List<Claim> findByUserIdOrderBySubmittedAtDesc(Long userId);
    
    // Claims for a specific policy (user view — filtered by userId for security)
    List<Claim> findByUserIdAndPolicyNumberOrderBySubmittedAtDesc(Long userId, String policyNumber);

    // Admin: claims for any policy number
    List<Claim> findByPolicyNumberOrderBySubmittedAtDesc(String policyNumber);

    // Admin: filter all claims by status
    List<Claim> findByStatusOrderBySubmittedAtDesc(ClaimStatus status);

    // Lookup by unique claim number
    Optional<Claim> findByClaimNumber(String claimNumber);
    
    // Count for claim number generation
    long countByClaimNumberStartingWith(String prefix);


}
