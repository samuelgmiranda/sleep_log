package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.model.SleepLog
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class JdbcSleepLogRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : SleepLogRepository {

    override fun existsForUserAndSleepDate(userId: Int, sleepDate: LocalDate): Boolean {
        val parameters = MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("startOfDay", sleepDate.atStartOfDay())
            .addValue("nextDay", sleepDate.plusDays(1).atStartOfDay())

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
            .addValue("userFeel", sleepLog.userFeel.databaseValue)

        jdbcTemplate.update(
            """
            INSERT INTO sleep_log (user_id, start_date, end_date, total_time, user_feel)
            VALUES (:userId, :startDate, :endDate, :totalTime, :userFeel)
            """.trimIndent(),
            parameters
        )
    }
}
