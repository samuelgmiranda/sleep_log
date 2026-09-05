package com.noom.interview.fullstack.sleep.dao

import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.util.DateUtil
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class SleepLogDAOImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : SleepLogDAO {

    override fun existsForUserAndSleepDate(userId: Int, sleepDate: LocalDate): Boolean {
        val parameters = MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("startOfDay", DateUtil.startOfDay(sleepDate))
            .addValue("nextDay", DateUtil.startOfNextDay(sleepDate))

        return jdbcTemplate.queryForObject(
            """
            SELECT EXISTS (
                SELECT 1
                FROM sleep_log
                WHERE user_id = :userId
                  AND start_date >= :startOfDay
                  AND start_date < :nextDay
            )
            """.trimIndent(),
            parameters,
            Boolean::class.java
        ) ?: false
    }

    override fun save(sleepLog: SleepLog) {
        val parameters = MapSqlParameterSource()
            .addValue("userId", sleepLog.userId)
            .addValue("startDate", sleepLog.startDate)
            .addValue("endDate", sleepLog.endDate)
            .addValue("totalTime", sleepLog.totalTime)
            .addValue("userFeel", sleepLog.userFeel)

        jdbcTemplate.update(
            """
            INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel)
            VALUES (:userId, :startDate, :endDate, :totalTime, :userFeel)
            """.trimIndent(),
            parameters
        )
    }

    override fun findByUserAndStartDateBetween(userId: Int, startOfDay: LocalDateTime, nextDay: LocalDateTime): SleepLog? {
        val parameters = MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("startOfDay", startOfDay)
            .addValue("nextDay", nextDay)

        return jdbcTemplate.query(
            """
            SELECT id, user_id, start_date, end_date, total_time, user_feel
            FROM sleep_log
            WHERE user_id = :userId
              AND start_date >= :startOfDay
              AND start_date < :nextDay
            ORDER BY start_date
            LIMIT 1
            """.trimIndent(), parameters
        ) { rs, _ ->
            SleepLog(rs.getInt("user_id"), rs.getTimestamp("start_date").toLocalDateTime(), rs.getTimestamp("end_date").toLocalDateTime(), rs.getLong("total_time"), rs.getInt("user_feel"), rs.getInt("id"))
        }.firstOrNull()
    }

    override fun findAllByUserAndStartDateBetween(
        userId: Int,
        startOfDay: LocalDateTime,
        nextDay: LocalDateTime
    ): List<SleepLog> {
        val parameters = MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("startOfDay", startOfDay)
            .addValue("nextDay", nextDay)

        return jdbcTemplate.query(
            """
            SELECT id, user_id, start_date, end_date, total_time, user_feel
            FROM sleep_log
            WHERE user_id = :userId
              AND start_date >= :startOfDay
              AND start_date < :nextDay
            ORDER BY start_date
            """.trimIndent(), parameters
        ) { rs, _ ->
            SleepLog(
                rs.getInt("user_id"),
                rs.getTimestamp("start_date").toLocalDateTime(),
                rs.getTimestamp("end_date").toLocalDateTime(),
                rs.getLong("total_time"),
                rs.getInt("user_feel"),
                rs.getInt("id")
            )
        }
    }
}
