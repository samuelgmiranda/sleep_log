-- shouldAggregateTimesImmediatelyAroundMidnight (user 6)
WITH pattern(position, start_time, end_time) AS (
    VALUES (0, TIME '23:45', TIME '07:45'), (1, TIME '23:50', TIME '07:50'),
           (2, TIME '23:55', TIME '07:55'), (3, TIME '00:05', TIME '08:05'),
           (4, TIME '00:10', TIME '08:10'), (5, TIME '00:15', TIME '08:15')
), source AS (
    SELECT 6 AS user_id, DATE '2026-05-01' + day_number AS sleep_day, p.start_time, p.end_time, day_number % 3 + 1 AS user_feel
    FROM generate_series(0, 29) AS day_number JOIN pattern p ON p.position = day_number % 6
), timestamps AS (
    SELECT *, sleep_day + start_time AS start_date, sleep_day + end_time + CASE WHEN end_time <= start_time THEN INTERVAL '1 day' ELSE INTERVAL '0 day' END AS end_date FROM source
)
INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel)
SELECT user_id, start_date, end_date, EXTRACT(EPOCH FROM end_date - start_date) / 60, user_feel FROM timestamps;
