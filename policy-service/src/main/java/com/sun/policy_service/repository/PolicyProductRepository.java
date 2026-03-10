package com.sun.policy_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sun.policy_service.entity.PolicyProduct;
import com.sun.policy_service.entity.PolicyType;
import java.util.List;

@Repository
public interface PolicyProductRepository extends JpaRepository<PolicyProduct, Long> {
	
	
	// Find all active policy products (for users to browse)
	List<PolicyProduct> findByActiveTrue();
	
	// Find active policy products by type (for users to filter)
	List<PolicyProduct> findByActiveTrueAndType(PolicyType type);
	
	// Check if a policy product with the same name already exists (for admin validation)
	boolean existsByName(String name);
	

}
