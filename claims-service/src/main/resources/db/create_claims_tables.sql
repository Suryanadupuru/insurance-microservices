-- ─────────────────────────────────────────────────────────────────────────────
-- create_claims_tables.sql
-- Schema for claims-service
-- Database: insurance_microservices_system
-- Table: claims
--
-- Note: user_policies is read from but NOT created here (owned by policy-service).
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS claims (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Ownership
    user_id                 BIGINT          NOT NULL,
    user_email              VARCHAR(150)    NOT NULL,

    -- Policy reference
    user_policy_id          BIGINT          NOT NULL,
    policy_number           VARCHAR(50)     NOT NULL,

    -- Claim identity
    claim_number            VARCHAR(50)     NOT NULL UNIQUE,

    -- Incident details
    claim_type              VARCHAR(30)     NOT NULL,
    incident_date           DATE            NOT NULL,
    incident_description    VARCHAR(2000)   NOT NULL,
    incident_location       VARCHAR(500),

    -- Financials
    claim_amount            DECIMAL(15, 2)  NOT NULL,
    approved_amount         DECIMAL(15, 2),

    -- Status & resolution
    status                  VARCHAR(20)     NOT NULL DEFAULT 'SUBMITTED',
    admin_notes             VARCHAR(1000),
    rejection_reason        VARCHAR(1000),
    reviewed_at             DATETIME,
    paid_at                 DATETIME,

    -- Audit
    submitted_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_claim_user_id       (user_id),
    INDEX idx_claim_policy_number (policy_number),
    INDEX idx_claim_status        (status),
    INDEX idx_claim_submitted_at  (submitted_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;