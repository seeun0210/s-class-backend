ALTER TABLE lessons
    ADD COLUMN google_calendar_event_id VARCHAR(256) NULL,
    ADD COLUMN google_meet_join_url VARCHAR(512) NULL,
    ADD COLUMN google_meet_code VARCHAR(64) NULL;