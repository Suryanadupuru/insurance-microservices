package com.sun.user_service.controller;

import com.sun.user_service.dto.request.ChangePasswordRequest;
import com.sun.user_service.dto.request.UpdateProfileRequest;
import com.sun.user_service.dto.response.MessageResponse;
import com.sun.user_service.dto.response.UserResponse;
import com.sun.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * UserController — user profile management.
 *
 * Identity comes from gateway-forwarded headers (set in SecurityContext
 * by GatewayHeaderFilter), NOT from JWT or @AuthenticationPrincipal.
 *
 * Header    → populated by
 * X-User-Id → gateway after JWT validation → stored in authentication.details
 * X-Username → gateway → authentication.principal (email)
 * X-User-Role → gateway → authentication.authorities
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management endpoints")
public class UserController {

    private final UserService userService;

    //GET /users/me

    @GetMapping("/me")
    @Operation(summary = "Get current user's profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyProfile(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(userService.toResponse(userService.getUserById(userId)));
    }

    //PUT /users/me

    @PutMapping("/me")
    @Operation(summary = "Update current user's profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    //POST /users/me/change-password

    @PostMapping("/me/change-password")
    @Operation(summary = "Change password for the authenticated user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(
                userId,
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        return ResponseEntity.ok(MessageResponse.of("Password changed successfully."));
    }

    //GET /users/{id} (Admin only)

    @GetMapping("/{id}")
    @Operation(summary = "Get any user by ID — Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toResponse(userService.getUserById(id)));
    }
}