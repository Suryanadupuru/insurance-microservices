package com.sun.auth.controller;

import com.sun.auth.dto.request.*;
import com.sun.auth.dto.response.AuthResponse;
import com.sun.auth.dto.response.MessageResponse;
import com.sun.auth.dto.response.RegisterResponse;
import com.sun.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — all authentication endpoints.
 * All routes are public (no JWT required).
 * Base path: /auth
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token management, password reset")
public class AuthController {

    private final AuthService authService;

    // ── POST /auth/register ───────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(
        summary     = "Register a new user",
        description = "Creates account and sends an email verification link."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    // ── GET /auth/verify-email?token= ─────────────────────────────────────────

    @GetMapping("/verify-email")
    @Operation(
        summary     = "Verify email address",
        description = "Activates account using the token sent in the verification email."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(
                MessageResponse.of("Email verified successfully! You can now log in."));
    }

    // ── POST /auth/login ──────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(
        summary     = "Login",
        description = "Authenticates user and returns JWT accessToken + refreshToken."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "403", description = "Account not verified"),
        @ApiResponse(responseCode = "423", description = "Account locked")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ── POST /auth/refresh ────────────────────────────────────────────────────

    @PostMapping("/refresh")
    @Operation(
        summary     = "Refresh access token",
        description = "Issues a new accessToken using a valid refreshToken. No re-login required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    // ── POST /auth/forgot-password ────────────────────────────────────────────

    @PostMapping("/forgot-password")
    @Operation(
        summary     = "Initiate password reset",
        description = "Sends a password reset link to the provided email. " +
                      "Always returns 200 to avoid revealing whether the email exists."
    )
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of(
                "If this email is registered, a reset link has been sent."));
    }

    // ── POST /auth/reset-password ─────────────────────────────────────────────

    @PostMapping("/reset-password")
    @Operation(
        summary     = "Reset password",
        description = "Sets a new password using the token received in the reset email."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid/expired token or password mismatch")
    })
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(
                request.getToken(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );
        return ResponseEntity.ok(MessageResponse.of(
                "Password reset successfully. Please log in with your new password."));
    }

    // ── POST /auth/resend-verification ────────────────────────────────────────

    @PostMapping("/resend-verification")
    @Operation(
        summary     = "Resend email verification link",
        description = "Resends the verification email for accounts that are not yet verified."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Verification email resent"),
        @ApiResponse(responseCode = "400", description = "Account already verified or not found")
    })
    public ResponseEntity<MessageResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.getEmail());
        return ResponseEntity.ok(MessageResponse.of(
                "Verification email resent. Please check your inbox."));
    }
}