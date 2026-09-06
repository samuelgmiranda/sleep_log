-- shouldCeilingFractionalMinuteAcrossMidnight (user 12)
INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel) VALUES
    (12, TIMESTAMP '2026-01-01 23:59:00', TIMESTAMP '2026-01-02 07:00:00', 421, 1),
    (12, TIMESTAMP '2026-01-02 00:00:00', TIMESTAMP '2026-01-02 07:00:00', 420, 2),
    (12, TIMESTAMP '2026-01-03 00:00:00', TIMESTAMP '2026-01-03 07:00:00', 420, 3);
