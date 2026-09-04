CREATE TABLE sleep_log (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    total_time INTEGER NOT NULL,
    user_feel INTEGER NOT NULL
);

CREATE INDEX idx_sleep_log_user_start_date
    ON sleep_log (user_id, start_date);
