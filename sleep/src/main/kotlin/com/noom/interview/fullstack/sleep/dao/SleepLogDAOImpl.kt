package com.noom.interview.fullstack.sleep.dao

import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.util.DateUtil
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate

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
}