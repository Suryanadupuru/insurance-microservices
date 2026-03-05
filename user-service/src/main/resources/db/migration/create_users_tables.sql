-- ─────────────────────────────────────────────────────────────────────────────
-- V1__create_users_tables.sql
-- Initial schema for the User Service database: insurance_users
--
-- Run by Flyway automatically on startup.
-- Switch application.yml ddl-auto from 'create' to 'validate' after first run.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name                  VARCHAR(100)    NOT NULL,
    last_name                   VARCHAR(100)    NOT NULL,
    email                       VARCHAR(150)    NOT NULL UNIQUE,
    password                    VARCHAR(255)    NOT NULL,      -- BCrypt hash
    phone_number                VARCHAR(15),
    address                     VARCHAR(255),

    -- Account Status
    status                      VARCHAR(30)     NOT NULL DEFAULT 'PENDING_VERIFICATION',
    enabled                     BOOLEAN         NOT NULL DEFAULT FALSE,
    account_non_locked          BOOLEAN         NOT NULL DEFAULT TRUE,
    account_non_expired         BOOLEAN         NOT NULL DEFAULT TRUE,
    failed_login_attempts       INT             NOT NULL DEFAULT 0,
    locked_until                DATETIME,

    -- Email Verification
    verification_token          VARCHAR(100),
    verification_token_expiry   DATETIME,

    -- Password Reset
    password_reset_token        VARCHAR(100),
    password_reset_token_expiry DATETIME,

    -- Audit
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at               DATETIME,

    INDEX idx_user_email (email),
    INDEX idx_verification_token (verification_token),
    INDEX idx_reset_token (password_reset_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS user_roles (
    user_id     BIGINT      NOT NULL,
    role        VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────────────────────────────────────
-- Seed: create a default admin user (password: Admin@1234)
-- Change the password immediately in production!
-- $2a$12$QCzbvU9/e0sDFrhVtXFK5.hUsn0tq.H1SjIvJ/4ruYTuML3Zd0pa.
-- ─────────────────────────────────────────────────────────────────────────────

INSERT IGNORE INTO users (
    first_name, last_name, email, password,
    status, enabled, account_non_locked, account_non_expired
) VALUES (
    'System', 'Admin',
    'admin@insuranceapp.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- Admin@1234
    'ACTIVE', TRUE, TRUE, TRUE
);

INSERT IGNORE INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE email = 'admin@insuranceapp.com';

INSERT IGNORE INTO user_roles (user_id, role)
SELECT id, 'USER' FROM users WHERE email = 'admin@insuranceapp.com';
