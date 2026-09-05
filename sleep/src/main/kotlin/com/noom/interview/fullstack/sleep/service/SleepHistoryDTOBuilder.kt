package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.model.SleepHistoryDTO
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.util.DateUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SleepHistoryDTOBuilder @Autowired constructor(
    private val sleepHistoryAggregationService: SleepHistoryAggregationService
) {

    constructor() : this(SleepHistoryAggregationService())

    fun forUser(userId: Int) = Builder(userId, sleepHistoryAggregationService)

    class Builder(
        private val userId: Int,
        private val sleepHistoryAggregationService: SleepHistoryAggregationService
    ) {
        private lateinit var dateRangeStart: String
        private lateinit var dateRangeEnd: String
        private lateinit var averageDuration: String
        private lateinit var averageStart: String
        private lateinit var averageEnd: String
        private lateinit var userFeelTotals: Map<String, Int>

        fun addRangeStart(startDate: LocalDate) = apply {
            dateRangeStart = DateUtil.formatDate(startDate, DateUtil.SHORT_MONTH_FORMATTER)
        }

        fun addRangeEnd(endDate: LocalDate) = apply {
            dateRangeEnd = DateUtil.formatDate(endDate, DateUtil.SHORT_MONTH_FORMATTER)
        }

        fun withAverageDuration(sleepLogs: List<SleepLog>) = apply {
            averageDuration = sleepHistoryAggregationService.averageDuration(sleepLogs)
        }

        fun withAverageStart(sleepLogs: List<SleepLog>) = apply {
            averageStart = sleepHistoryAggregationService.averageBedTime(sleepLogs)
        }

        fun withAverageEnd(sleepLogs: List<SleepLog>) = apply {
            averageEnd = sleepHistoryAggregationService.averageWakeTime(sleepLogs)
        }

        fun withUserFeelTotals(sleepLogs: List<SleepLog>) = apply {
            userFeelTotals = sleepHistoryAggregationService.feelingTotals(sleepLogs)
        }

        fun build() = SleepHistoryDTO(
            userId,
            dateRangeStart,
            dateRangeEnd,
            averageDuration,
            averageStart,
            averageEnd,
            userFeelTotals
        )
    }
}
