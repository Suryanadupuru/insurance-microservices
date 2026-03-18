-- ─────────────────────────────────────────────────────────────────────────────
-- create_support_tables.sql
-- Schema for support-service
-- Database: insurance_microservices_system
-- Table: support_tickets
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS support_tickets (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Ownership
    user_id             BIGINT          NOT NULL,
    user_email          VARCHAR(150)    NOT NULL,

    -- Ticket identity
    ticket_number       VARCHAR(50)     NOT NULL UNIQUE,

    -- Request details
    subject             VARCHAR(200)    NOT NULL,
    description         VARCHAR(3000)   NOT NULL,
    category            VARCHAR(30)     NOT NULL,   -- POLICY_INQUIRY, CLAIMS_INQUIRY, BILLING, TECHNICAL, ACCOUNT, GENERAL
    reference_number    VARCHAR(50),                -- optional policy or claim number

    -- Admin fields
    priority            VARCHAR(10)     NOT NULL DEFAULT 'MEDIUM',
    status              VARCHAR(15)     NOT NULL DEFAULT 'OPEN',
    admin_notes         VARCHAR(2000),
    resolution_notes    VARCHAR(2000),
    resolved_at         DATETIME,
    closed_at           DATETIME,

    -- Audit
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_ticket_user_id   (user_id),
    INDEX idx_ticket_status    (status),
    INDEX idx_ticket_category  (category),
    INDEX idx_ticket_priority  (priority),
    INDEX idx_ticket_created   (created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;