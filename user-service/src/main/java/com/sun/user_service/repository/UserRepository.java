package com.sun.user_service.repository;

import com.sun.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — Data Access Layer
 *
 * Extends JpaRepository to provide standard CRUD operations.
 * Custom query methods are derived from method naming conventions (Spring Data JPA).
 *
 * Base methods inherited from JpaRepository:
 *  - save(User user)
 *  - findById(Long id)
 *  - findAll()
 *  - deleteById(Long id)
 *  - existsById(Long id)
 *  ... and more
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Check if a user already exists with the given email.
     * Used during registration to prevent duplicate accounts.
     *
     * @param email the email to check
     * @return true if a user with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find a user by their email address.
     * Used for login, profile retrieval, and password reset initiation.
     *
     * @param email the user's email
     * @return an Optional containing the User if found
     */
    Optional<User> findByEmail(String email);
}
