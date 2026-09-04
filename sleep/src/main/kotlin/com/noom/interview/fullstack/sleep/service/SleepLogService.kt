package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.api.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

@Service
class SleepLogService(
    private val sleepLogRepository: SleepLogRepository
) {

    fun createSleepLog(userId: Int, request: CreateSleepLogRequest) {
        val startDate = parseDate(request.startDate, "startDate")
        val endDate = parseDate(request.endDate, "endDate")
        val userFeel = request.userFeel
            ?: throw badRequest("userFeel is required")

        if (!endDate.isAfter(startDate)) {
            throw badRequest("endDate must be after startDate")
        }

        val sleepDate = startDate.toLocalDate()
        if (sleepLogRepository.existsForUserAndSleepDate(userId, sleepDate)) {
            throw badRequest("A sleep log already exists for this sleep date")
        }

        sleepLogRepository.save(
            SleepLog(
                userId = userId,
                startDate = startDate,
                endDate = endDate,
                totalTime = Duration.between(startDate, endDate).toMinutes(),
                userFeel = userFeel
            )
        )
    }

    private fun parseDate(value: String?, fieldName: String): LocalDateTime {
        if (value == null) {
            throw badRequest("$fieldName is required")
        }

        return try {
            LocalDateTime.parse(value, DATE_TIME_FORMATTER)
        } catch (exception: DateTimeParseException) {
            throw badRequest("$fieldName must use MM/dd/yyyy HH:mm format")
        }
    }

    private fun badRequest(reason: String): ResponseStatusException =
        ResponseStatusException(HttpStatus.BAD_REQUEST, reason)

    private companion object {
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("MM/dd/uuuu HH:mm")
            .withResolverStyle(ResolverStyle.STRICT)
    }
}
