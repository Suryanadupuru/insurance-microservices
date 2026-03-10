-- ─────────────────────────────────────────────────────────────────────────────
-- create_policy_tables.sql
-- Schema for policy-service
-- Database: insurance_microservices_system
-- Tables: policy_products, user_policies
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS policy_products (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Product details
    name             VARCHAR(150)        NOT NULL,
    description      VARCHAR(1000)       NOT NULL,
    type             VARCHAR(30)         NOT NULL,   -- HEALTH, LIFE, VEHICLE, HOME, TRAVEL

    -- Coverage and pricing
    coverage_amount  DECIMAL(15, 2)      NOT NULL,
    monthly_premium  DECIMAL(10, 2)      NOT NULL,
    annual_premium   DECIMAL(10, 2)      NOT NULL,
    duration_months  INT                 NOT NULL,

    -- Eligibility
    min_age          INT,
    max_age          INT,

    -- Status
    active           BOOLEAN             NOT NULL DEFAULT TRUE,

    -- Audit
    created_at       DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_policy_product_type   (type),
    INDEX idx_policy_product_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS user_policies (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Ownership
    user_id             BIGINT              NOT NULL,
    user_email          VARCHAR(150)        NOT NULL,

    -- Policy reference
    policy_product_id   BIGINT              NOT NULL,
    policy_number       VARCHAR(50)         NOT NULL UNIQUE,

    -- Coverage snapshot at time of enrollment
    coverage_amount     DECIMAL(15, 2)      NOT NULL,
    premium_paid        DECIMAL(10, 2)      NOT NULL,

    -- Dates
    start_date          DATE                NOT NULL,
    end_date            DATE                NOT NULL,

    -- Status
    status              VARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    cancellation_reason VARCHAR(500),
    cancelled_at        DATETIME,

    -- Audit
    created_at          DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user_policy_user_id       (user_id),
    INDEX idx_user_policy_status        (status),
    INDEX idx_user_policy_end_date      (end_date),

    CONSTRAINT fk_user_policy_product
        FOREIGN KEY (policy_product_id) REFERENCES policy_products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────────────────────────────────────
-- Seed: sample policy products
-- ─────────────────────────────────────────────────────────────────────────────

INSERT IGNORE INTO policy_products
    (name, description, type, coverage_amount, monthly_premium, annual_premium, duration_months, min_age, max_age)
VALUES
    ('Basic Health Plan',
     'Essential health coverage including hospitalization and outpatient care.',
     'HEALTH', 500000.00, 1500.00, 16500.00, 12, 18, 60),

    ('Comprehensive Life Cover',
     'Life insurance with term coverage and family protection benefits.',
     'LIFE', 2000000.00, 2500.00, 27500.00, 24, 21, 55),

    ('Vehicle Shield',
     'Comprehensive vehicle insurance covering accidents, theft, and third-party liability.',
     'VEHICLE', 300000.00, 800.00, 8800.00, 12, 18, 70),

    ('Home Protect Plus',
     'Home insurance covering structure, contents, and natural disasters.',
     'HOME', 1000000.00, 1200.00, 13200.00, 12, 21, 70),

    ('Travel Guard',
     'International travel insurance with medical, trip cancellation, and baggage cover.',
     'TRAVEL', 100000.00, 300.00, 3300.00, 1, 18, 75);