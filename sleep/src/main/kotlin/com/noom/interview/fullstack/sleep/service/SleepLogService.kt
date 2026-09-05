package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.controller.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.exception.BusinessValidationException
import com.noom.interview.fullstack.sleep.exception.ResourceNotFoundException
import com.noom.interview.fullstack.sleep.dao.SleepLogDAO
import com.noom.interview.fullstack.sleep.model.SleepHistoryDTO
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.model.SleepLogDTO
import com.noom.interview.fullstack.sleep.model.UserFeel
import com.noom.interview.fullstack.sleep.util.DateUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SleepLogService @Autowired constructor(
    private val sleepLogDAO: SleepLogDAO,
    private val sleepHistoryDTOBuilder: SleepHistoryDTOBuilder
) {

    constructor(sleepLogDAO: SleepLogDAO) : this(sleepLogDAO, SleepHistoryDTOBuilder())

    fun createSleepLog(userId: Int, request: CreateSleepLogRequest) {
        val startDate = DateUtil.parseDate(request.startDate!!)
        val endDate = DateUtil.parseDate(request.endDate!!)
        val userFeel = UserFeel.valueOf(request.userFeel!!).databaseValue

        val sleepDate = DateUtil.sleepDate(startDate)
        if (sleepLogDAO.existsForUserAndSleepDate(userId, sleepDate)) {
            throw BusinessValidationException("A sleep log already exists for this sleep date")
        }

        sleepLogDAO.save(
            SleepLog(
                userId = userId,
                startDate = startDate,
                endDate = endDate,
                totalTime = DateUtil.minutesBetween(startDate, endDate),
                userFeel = userFeel
            )
        )
    }

    fun getSleepLog(userId: Int, targetDate: LocalDate?): SleepLogDTO {
        val date = targetDate ?: LocalDate.now().minusDays(1)
        val sleepLog = sleepLogDAO.findByUserAndStartDateBetween(userId, DateUtil.startOfDay(date), DateUtil.startOfNextDay(date))
            ?: throw ResourceNotFoundException("Sleep log not found")
        return SleepLogDTO(
            id = sleepLog.id!!,
            targetDate = DateUtil.formatDate(date),
            sleepDuration = DateUtil.formatDuration(sleepLog.totalTime),
            startSleep = DateUtil.formatTime(sleepLog.startDate),
            endSleep = DateUtil.formatTime(sleepLog.endDate),
            userFeel = UserFeel.values().first { it.databaseValue == sleepLog.userFeel }.name
        )
    }

    fun getSleepHistory(userId: Int, historyDays: Int): SleepHistoryDTO {
        val currentDate = DateUtil.currentLocalDate()
        val queryEndDate = DateUtil.historyEndDate(currentDate)
        val startDate = DateUtil.historyStartDate(queryEndDate, historyDays)
        val sleepLogs = sleepLogDAO.findAllByUserAndStartDateBetween(
            userId,
            DateUtil.startOfDay(startDate),
            DateUtil.startOfNextDay(queryEndDate)
        )

        if (sleepLogs.isEmpty()) {
            throw ResourceNotFoundException("Sleep history not found")
        }

        return buildSleepHistory(userId, startDate, currentDate, sleepLogs)
    }

    private fun buildSleepHistory(
        userId: Int,
        startDate: LocalDate,
        responseEndDate: LocalDate,
        sleepLogs: List<SleepLog>
    ): SleepHistoryDTO =
        sleepHistoryDTOBuilder.forUser(userId)
            .addRangeStart(startDate)
            .addRangeEnd(responseEndDate)
            .withAverageDuration(sleepLogs)
            .withAverageStart(sleepLogs)
            .withAverageEnd(sleepLogs)
            .withUserFeelTotals(sleepLogs)
            .build()
}
