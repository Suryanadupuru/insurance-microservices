package com.sun.auth.service;

import com.sun.auth.dto.request.LoginRequest;
import com.sun.auth.dto.request.RegisterRequest;
import com.sun.auth.dto.response.AuthResponse;
import com.sun.auth.dto.response.RegisterResponse;
import com.sun.auth.entity.AccountStatus;
import com.sun.auth.entity.User;
import com.sun.auth.exception.*;
import com.sun.auth.repository.UserRepository;
import com.sun.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * AuthService — core business logic for all auth operations.
 *
 * Operations:
 *  - register        : create user, send verification email
 *  - verifyEmail     : activate account via token
 *  - login           : authenticate, issue JWT access + refresh tokens
 *  - refreshToken    : silent re-auth using refresh token
 *  - logout          : revoke refresh token in DB
 *  - forgotPassword  : send reset link
 *  - resetPassword   : set new password via reset token
 *  - resendVerification
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS  = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    private static final int VERIFICATION_EXPIRY_HOURS = 24;
    private static final int RESET_TOKEN_EXPIRY_HOURS  = 1;

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final EmailService          emailService;
    private final AuthenticationManager authenticationManager;

    // ── Register ──────────────────────────────────────────────────────────────

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .status(AccountStatus.PENDING_VERIFICATION)
                .enabled(false)
                .roles(Set.of("USER"))
                .verificationToken(verificationToken)
                .verificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_EXPIRY_HOURS))
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());

        emailService.sendVerificationEmail(saved.getEmail(), saved.getFullName(), verificationToken);

        return RegisterResponse.builder()
                .userId(saved.getId())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .message("Registration successful! Please check your email to verify your account.")
                .build();
    }

    // ── Email Verification ────────────────────────────────────────────────────

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token."));

        if (user.isVerificationTokenExpired()) {
            throw new InvalidTokenException(
                "Verification token has expired. Please request a new one.");
        }

        user.setEnabled(true);
        user.setStatus(AccountStatus.ACTIVE);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getEmail());
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        // Check lock — auto-unlock if lock period has expired
        if (!user.isAccountNonLocked()) {
            if (user.getLockedUntil() != null &&
                    LocalDateTime.now().isAfter(user.getLockedUntil())) {
                user.resetFailedLoginAttempts();
                userRepository.save(user);
            } else {
                throw new AccountLockedException(
                    "Account locked due to too many failed attempts. " +
                    "Try again after " + LOCK_DURATION_MINUTES + " minutes or reset your password.");
            }
        }

        // Check email verification
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException();
        }

        // Authenticate
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException();
        } catch (DisabledException e) {
            throw new AccountNotVerifiedException();
        } catch (LockedException e) {
            throw new AccountLockedException("Account is locked.");
        }

        // Success — reset counter, update last login
        user.resetFailedLoginAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles())
                .build();
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        final String email;
        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid refresh token.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found for refresh token."));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new InvalidTokenException("Refresh token is expired or invalid.");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        log.info("Access token refreshed for: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)    // same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles())
                .build();
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    public void forgotPassword(String email) {
        // ifPresent — never reveal whether email exists
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(
                LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
            log.info("Password reset initiated for: {}", email);
        });
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException();
        }

        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token."));

        if (user.isPasswordResetTokenExpired()) {
            throw new InvalidTokenException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset for user: {}", user.getEmail());
    }

    // ── Resend Verification ───────────────────────────────────────────────────

    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No account found for: " + email));

        if (user.isEnabled()) {
            throw new InvalidTokenException("Account is already verified.");
        }

        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_EXPIRY_HOURS));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), newToken);
        log.info("Verification email resent to: {}", email);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void handleFailedLogin(User user) {
        user.incrementFailedLoginAttempts();
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("Account locked for: {} after {} failed attempts",
                    user.getEmail(), MAX_FAILED_ATTEMPTS);
        }
        userRepository.save(user);
    }
}