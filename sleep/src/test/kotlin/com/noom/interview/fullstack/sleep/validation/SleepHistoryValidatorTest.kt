package com.noom.interview.fullstack.sleep.validation

import org.junit.jupiter.api.assertDoesNotThrow
import com.noom.interview.fullstack.sleep.exception.InvalidRequestException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SleepHistoryValidatorTest {

    private val validator = SleepHistoryValidator()

    @Test
    fun acceptsHistoryDaysValue() {
        var historyDays = "10";

        assertDoesNotThrow {
            validator.validateHistoryDays(historyDays);
        }
    }

    @Test
    fun rejectsHistoryDaysValueOutOfRange() {
        var historyDaysZero = "0";

        assertThrows (InvalidRequestException::class.java) {
            validator.validateHistoryDays(historyDaysZero);
        }

        var historyDaysAboveMax = "366";

        assertThrows (InvalidRequestException::class.java) {
            validator.validateHistoryDays(historyDaysAboveMax);
        }
    }

    @Test
    fun nullableHistoryDaysValue() {
        var historyDaysNull = null;
        var defaultHistoryDays = validator.validateHistoryDays(historyDaysNull);
        assertEquals(30, defaultHistoryDays);
    }

}