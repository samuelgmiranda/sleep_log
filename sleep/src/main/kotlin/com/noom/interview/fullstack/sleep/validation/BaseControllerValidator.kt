package com.noom.interview.fullstack.sleep.validation

import com.noom.interview.fullstack.sleep.exception.InvalidRequestException
import org.springframework.stereotype.Component

@Component
class BaseControllerValidator {

    fun validateUserId(value: String?): Int {
        if (value == null) {
            throw InvalidRequestException("X-User-Id header is required")
        }

        return value.toIntOrNull()
            ?: throw InvalidRequestException("X-User-Id header must be a valid integer")
    }
}