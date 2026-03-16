package com.sun.claims_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sun.claims_service.entity.UserPolicy;

/**
 * Read-only access to user_policies (owned by policy-service).
 * Used solely to validate a policy before accepting a claim.
 */

@Repository
public interface UserPolicyRepository extends JpaRepository<UserPolicy, Long>{
	
	Optional<UserPolicy> findByPolicyNumberAndUserId(String policyNumber, Long userId);
	
}
