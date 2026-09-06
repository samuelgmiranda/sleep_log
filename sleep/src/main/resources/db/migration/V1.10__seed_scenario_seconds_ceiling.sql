-- shouldCeilingTimestampSecondsBeforeClockAggregation (user 11)
INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel) VALUES
    (11, TIMESTAMP '2026-01-01 23:59:59', TIMESTAMP '2026-01-02 00:00:01', 0, 1),
    (11, TIMESTAMP '2026-01-02 00:00:01', TIMESTAMP '2026-01-02 23:59:59', 1439, 2);
