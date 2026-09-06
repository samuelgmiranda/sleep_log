-- shouldPreferMidnightWhenCircularTimeGapsTie (user 10)
INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel) VALUES
    (10, TIMESTAMP '2026-01-01 06:00:00', TIMESTAMP '2026-01-01 07:00:00', 60, 1),
    (10, TIMESTAMP '2026-01-02 18:00:00', TIMESTAMP '2026-01-02 19:00:00', 60, 2);
