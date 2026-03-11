package com.sun.policy_search_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.sun.policy_search_service.entity.PolicyProduct;

/**
 * Read-only repository for policy_products.
 *
 * JpaSpecificationExecutor enables dynamic multi-criteria search
 * without writing a new query method for every combination of filters.
 */
public interface PolicySearchRepository 
			extends JpaRepository<PolicyProduct, Long>, 
					JpaSpecificationExecutor<PolicyProduct> {

}
