-- Teacher Google account connection schema.
-- Run before deploying the OAuth Google connection feature because
-- production services validate JPA mappings at startup.

CREATE TABLE IF NOT EXISTS teacher_google_accounts (
    id VARCHAR(26) NOT NULL,
    user_id VARCHAR(26) NOT NULL,
    google_email VARCHAR(320) NOT NULL,
    encrypted_refresh_token TEXT NOT NULL,
    scope VARCHAR(500) NOT NULL,
    connected_at DATETIME(6) NOT NULL,
    last_used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_teacher_google_user_id (user_id),
    KEY idx_teacher_google_email (google_email)
);
