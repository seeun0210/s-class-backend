-- Central Google account connection schema.
-- Run before deploying the central Google Calendar account feature because
-- production services validate JPA mappings at startup.

CREATE TABLE IF NOT EXISTS central_google_accounts (
    provider VARCHAR(20) NOT NULL,
    google_email VARCHAR(320) NOT NULL,
    encrypted_refresh_token TEXT NOT NULL,
    scope VARCHAR(500) NOT NULL,
    connected_by_admin_user_id VARCHAR(26) NOT NULL,
    connected_at DATETIME(6) NOT NULL,
    last_used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (provider),
    KEY idx_central_google_email (google_email)
);
