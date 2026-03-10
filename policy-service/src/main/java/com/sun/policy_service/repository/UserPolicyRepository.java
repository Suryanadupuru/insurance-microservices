package com.sun.policy_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sun.policy_service.entity.UserPolicy;
import com.sun.policy_service.entity.PolicyStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPolicyRepository extends JpaRepository<UserPolicy, Long> {
	
	// Find all policies for a specific user
	List<UserPolicy> findByUserId(Long userId);
	
	// Find policies for a user filtered by status (e.g. ACTIVE, EXPIRED)
	List<UserPolicy> findByUserIdAndStatus(Long userId, PolicyStatus status);
	
	// Find a specific policy by its unique policy number
	Optional<UserPolicy> findByPolicyNumber(String policyNumber);
	
	// Find policies by status and end date before now (for scheduled expiration processing and used by renewal service)
	List<UserPolicy> findByStatusAndEndDateBefore(PolicyStatus status, LocalDate date);

}
