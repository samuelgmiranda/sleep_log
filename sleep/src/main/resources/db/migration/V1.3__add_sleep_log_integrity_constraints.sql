ALTER TABLE sleep_log
    ADD CONSTRAINT chk_sleep_log_user_feel
        CHECK (user_feel IN (1, 2, 3)),
    ADD CONSTRAINT chk_sleep_log_end_date_after_start_date
        CHECK (end_date > start_date);
