package com.sun.user_service.service;

import com.sun.user_service.dto.request.UpdateProfileRequest;
import com.sun.user_service.dto.response.UserResponse;
import com.sun.user_service.entity.User;
import com.sun.user_service.exception.InvalidCredentialsException;
import com.sun.user_service.exception.PasswordMismatchException;
import com.sun.user_service.exception.UserNotFoundException;
import com.sun.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserService — manages user profiles only.
 *
 * All auth concerns (register, verify email, password reset, login tracking)
 * have been moved to auth-server. This service focuses purely on:
 *   1. Profile retrieval  (by ID or email)
 *   2. Profile update     (name, phone, address)
 *   3. Change password    (authenticated user changing their own password)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Profile Retrieval ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    // ── Profile Update ────────────────────────────────────────────────────────

    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        if (request.getFirstName()   != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()    != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress()     != null) user.setAddress(request.getAddress());

        User updated = userRepository.save(user);
        log.info("Profile updated for user id: {}", userId);

        return toResponse(updated);
    }

    // ── Change Password ───────────────────────────────────────────────────────

    public void changePassword(Long userId, String currentPassword,
                               String newPassword, String confirmPassword) {

        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException();
        }

        User user = getUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user id: {}", userId);
    }

    // ── Response Mapping ──────────────────────────────────────────────────────

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .status(user.getStatus().name())
                .enabled(user.isEnabled())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}