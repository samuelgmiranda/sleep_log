package com.noom.interview.fullstack.sleep.validation

import com.noom.interview.fullstack.sleep.controller.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.exception.InvalidRequestException
import com.noom.interview.fullstack.sleep.model.UserFeel
import com.noom.interview.fullstack.sleep.util.DateUtil
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import org.springframework.stereotype.Component

@Component
class SleepLogValidator {

    fun validateCreateRequest(request: CreateSleepLogRequest) {
        val startDate = parseDate(request.startDate, "startDate")
        val endDate = parseDate(request.endDate, "endDate")

        if (!endDate.isAfter(startDate)) {
            throw InvalidRequestException("endDate must be after startDate")
        }

        val userFeel = request.userFeel
            ?: throw InvalidRequestException("userFeel is required")

        if (UserFeel.values().none { it.name == userFeel }) {
            throw InvalidRequestException("invalid userFeel parameter")
        }
    }

    private fun parseDate(value: String?, fieldName: String): LocalDateTime {
        if (value == null) {
            throw InvalidRequestException("$fieldName is required")
        }

        return try {
            DateUtil.parseDate(value)
        } catch (exception: DateTimeParseException) {
            throw InvalidRequestException("$fieldName must use MM/dd/uuuu HH:mm format")
        }
    }

}
