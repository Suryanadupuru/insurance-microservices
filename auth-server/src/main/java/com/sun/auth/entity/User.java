package com.sun.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity — core domain model for the User Service.
 *
 * Personal data is stored with field-level encryption applied at the
 * service layer (AES-256) before persisting, satisfying the NFR
 * for data encryption outlined in the BRD section 5.3.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_verification_token", columnList = "verificationToken")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identity ─────────────────────────────────────────────────────────────

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;                // BCrypt hashed

    @Column(length = 15)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    // ── Account Status ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING_VERIFICATION;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonExpired = true;

    @Builder.Default
    private int failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    // ── Email Verification ────────────────────────────────────────────────────

    @Column(length = 100)
    private String verificationToken;

    private LocalDateTime verificationTokenExpiry;

    // ── Password Reset ────────────────────────────────────────────────────────

    @Column(length = 100)
    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiry;

    // ── Roles ─────────────────────────────────────────────────────────────────

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    // ── Audit ─────────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    // ── Helpers ───────────────────────────────────────────────────────────────

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isVerificationTokenExpired() {
        return verificationTokenExpiry != null &&
               LocalDateTime.now().isAfter(verificationTokenExpiry);
    }

    public boolean isPasswordResetTokenExpired() {
        return passwordResetTokenExpiry != null &&
               LocalDateTime.now().isAfter(passwordResetTokenExpiry);
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.accountNonLocked = true;
    }
}
