package com.noom.interview.fullstack.sleep.validation

import com.noom.interview.fullstack.sleep.exception.InvalidRequestException
import org.springframework.stereotype.Component

@Component
class SleepHistoryValidator {

    fun validateHistoryDays(historyDays: String?): Int {
        val effectiveHistoryDays = historyDays?.toIntOrNull()
            ?: if (historyDays == null) DEFAULT_HISTORY_DAYS else throw InvalidRequestException("historyDays must be an integer")
        if (effectiveHistoryDays <= 0) {
            throw InvalidRequestException("historyDays must be greater than zero")
        }
        return effectiveHistoryDays
    }

    private companion object {
        const val DEFAULT_HISTORY_DAYS = 30
    }
}
