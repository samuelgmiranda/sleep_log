-- Fixed API-test clock: 2026-09-05 America/Sao_Paulo.
-- User 5 intentionally remains without a 2026-09-04 record to retain exactly
-- one last-night not-found case in the matrix.
INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel) VALUES
    (6,  TIMESTAMP '2026-09-04 22:00:00', TIMESTAMP '2026-09-05 06:00:00', 480, 2),
    (7,  TIMESTAMP '2026-09-04 22:00:00', TIMESTAMP '2026-09-05 06:00:00', 480, 2),
    (8,  TIMESTAMP '2026-09-04 22:00:00', TIMESTAMP '2026-09-05 06:00:00', 480, 2),
    (9,  TIMESTAMP '2026-09-04 22:00:00', TIMESTAMP '2026-09-05 06:00:00', 480, 2),
    (10, TIMESTAMP '2026-09-04 22:00:00', TIMESTAMP '2026-09-05 06:00:00', 480, 2),
    (11, TIMESTAMP '2026-09-04 22:00:00', TIMESTAMP '2026-09-05 06:00:00', 480, 2),
    (12, TIMESTAMP '2026-09-04 22:00:00', TIMESTAMP '2026-09-05 06:00:00', 480, 2),
    (13, TIMESTAMP '2026-09-04 22:00:00', TIMESTAMP '2026-09-05 06:00:00', 480, 2),
    (14, TIMESTAMP '2026-09-04 22:00:00', TIMESTAMP '2026-09-05 06:00:00', 480, 2);

-- User 15 exercises every matrix window: one identical sleep log for each
-- of the 365 days included by the fixed API-test clock.
INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel)
SELECT
    15,
    DATE '2025-09-05' + day_number + TIME '22:00:00',
    DATE '2025-09-05' + day_number + 1 + TIME '06:00:00',
    480,
    2
FROM generate_series(0, 364) AS day_number;
