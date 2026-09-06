-- shouldAggregateCrossMidnightTimesAcrossYearBoundary (user 8)
WITH pattern(position, start_time, end_time) AS (
    VALUES (0, TIME '22:15', TIME '05:45'), (1, TIME '22:45', TIME '06:27'),
           (2, TIME '23:15', TIME '07:09'), (3, TIME '23:45', TIME '07:51'),
           (4, TIME '00:15', TIME '08:33'), (5, TIME '00:45', TIME '09:15')
), source AS (
    SELECT 8 AS user_id, DATE '2026-12-27' + day_number AS sleep_day, p.start_time, p.end_time, day_number % 3 + 1 AS user_feel
    FROM generate_series(0, 29) AS day_number JOIN pattern p ON p.position = day_number % 6
), timestamps AS (
    SELECT *, sleep_day + start_time AS start_date, sleep_day + end_time + CASE WHEN end_time <= start_time THEN INTERVAL '1 day' ELSE INTERVAL '0 day' END AS end_date FROM source
)
INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel)
SELECT user_id, start_date, end_date, EXTRACT(EPOCH FROM end_date - start_date) / 60, user_feel FROM timestamps;
