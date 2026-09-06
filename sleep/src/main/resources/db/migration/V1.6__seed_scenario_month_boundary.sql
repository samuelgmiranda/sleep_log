-- shouldAggregateDifferentDurationsAcrossMonthBoundary (user 7)
WITH pattern(position, start_time, end_time) AS (
    VALUES (0, TIME '21:00', TIME '03:30'), (1, TIME '21:30', TIME '04:30'),
           (2, TIME '22:00', TIME '05:30'), (3, TIME '22:30', TIME '06:30'),
           (4, TIME '23:00', TIME '07:30'), (5, TIME '23:30', TIME '08:30')
), source AS (
    SELECT 7 AS user_id, DATE '2026-01-27' + day_number AS sleep_day, p.start_time, p.end_time, day_number % 3 + 1 AS user_feel
    FROM generate_series(0, 29) AS day_number JOIN pattern p ON p.position = day_number % 6
), timestamps AS (
    SELECT *, sleep_day + start_time AS start_date, sleep_day + end_time + CASE WHEN end_time <= start_time THEN INTERVAL '1 day' ELSE INTERVAL '0 day' END AS end_date FROM source
)
INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel)
SELECT user_id, start_date, end_date, EXTRACT(EPOCH FROM end_date - start_date) / 60, user_feel FROM timestamps;
