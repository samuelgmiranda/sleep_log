package com.noom.interview.fullstack.sleep.validation

import com.noom.interview.fullstack.sleep.controller.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.exception.InvalidRequestException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class SleepLogValidatorTest {

    private val validator = SleepLogValidator()

    @Test
    fun acceptsValidCrossMidnightRequest() {
        assertDoesNotThrow { 
            validator.validateCreateRequest(
                CreateSleepLogRequest("09/03/2026 22:00", "09/04/2026 05:30", "GOOD")
            ) 
        }
        
    }

    @Test
    fun rejectsInvalidDate() {
        assertThrows(InvalidRequestException::class.java) {
            validator.validateCreateRequest(
                CreateSleepLogRequest("09/31/2026 22:00", "10/01/2026 05:30", "GOOD")
            )
        }
    }

    @Test
    fun rejectsInvalidFeeling() {
        assertThrows(InvalidRequestException::class.java) {
            validator.validateCreateRequest(
                CreateSleepLogRequest("09/03/2026 22:00", "09/04/2026 05:30", "GREAT")
            )
        }
    }

    @Test
    fun rejectsNonPositiveInterval() {
        assertThrows(InvalidRequestException::class.java) {
            validator.validateCreateRequest(
                CreateSleepLogRequest("09/04/2026 05:30", "09/04/2026 05:30", "OK")
            )
        }
    }

    @Test
    fun rejectsEndDateBeforeStartDate() {
        assertThrows(InvalidRequestException::class.java) {
            validator.validateCreateRequest(
                CreateSleepLogRequest("09/04/2026 05:30", "09/03/2026 22:00", "OK")
            )
        }
    }
}
