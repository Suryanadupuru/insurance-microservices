-- ─────────────────────────────────────────────────────────────────────────────
-- create_auth_tables.sql
-- Schema for auth-server.
-- Shares the same database (insurance_microservices_system) as other services
-- but uses its own table prefix: auth_users, auth_user_roles
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Identity
    first_name                  VARCHAR(100)    NOT NULL,
    last_name                   VARCHAR(100)    NOT NULL,
    email                       VARCHAR(150)    NOT NULL UNIQUE,
    password                    VARCHAR(255)    NOT NULL,       -- BCrypt hash (strength 12)
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

    INDEX idx_auth_user_email           (email),
    INDEX idx_auth_verification_token   (verification_token),
    INDEX idx_auth_reset_token          (password_reset_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS user_roles (
    user_id     BIGINT      NOT NULL,
    role        VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_auth_user_roles FOREIGN KEY (user_id)
        REFERENCES auth_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────────────────────────────────────
-- Seed: default admin user  (password: Admin@1234)
-- Change immediately in production!
-- ─────────────────────────────────────────────────────────────────────────────

INSERT IGNORE INTO auth_users (
    first_name, last_name, email, password,
    status, enabled, account_non_locked, account_non_expired
) VALUES (
    'System', 'Admin',
    'admin@insuranceapp.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',  -- Admin@1234
    'ACTIVE', TRUE, TRUE, TRUE
);

INSERT IGNORE INTO auth_user_roles (user_id, role)
SELECT id, 'ADMIN' FROM auth_users WHERE email = 'admin@insuranceapp.com';

INSERT IGNORE INTO auth_user_roles (user_id, role)
SELECT id, 'USER' FROM auth_users WHERE email = 'admin@insuranceapp.com';