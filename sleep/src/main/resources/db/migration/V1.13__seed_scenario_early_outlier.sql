-- shouldWeightCrossMidnightOutliersByTheirFrequency: mostly early (user 14)
INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel)
SELECT 14, TIMESTAMP '2026-02-01 01:00:00', TIMESTAMP '2026-02-01 09:00:00', 480, 2
FROM generate_series(1, 29)
UNION ALL SELECT 14, TIMESTAMP '2026-03-01 23:00:00', TIMESTAMP '2026-03-02 07:00:00', 480, 2;
