package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.model.UserFeel
import com.noom.interview.fullstack.sleep.util.DateUtil
import org.springframework.stereotype.Service

@Service
class SleepHistoryAggregationService {

    fun averageDuration(sleepLogs: List<SleepLog>): String =
        DateUtil.formatDuration(kotlin.math.ceil(sleepLogs.map { it.totalTime }.average()).toLong())

    fun averageBedTime(sleepLogs: List<SleepLog>): String =
        averageTime(sleepLogs.map { it.startDate })

    fun averageWakeTime(sleepLogs: List<SleepLog>): String =
        averageTime(sleepLogs.map { it.endDate })

    fun feelingTotals(sleepLogs: List<SleepLog>): Map<String, Int> =
        UserFeel.values().associate { feeling ->
            feeling.name to sleepLogs.count { it.userFeel == feeling.databaseValue }
        }

    private fun averageTime(dateTimes: List<java.time.LocalDateTime>): String {
        val minutes = dateTimes.map(DateUtil::minutesAfterMidnight)
        return DateUtil.formatClockTime(
            DateUtil.ceilingAverageMinute(DateUtil.normalizeMinutesAcrossMidnight(minutes))
        )
    }
}
