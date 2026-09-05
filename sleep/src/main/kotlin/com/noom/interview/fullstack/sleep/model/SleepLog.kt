package com.noom.interview.fullstack.sleep.model

import java.time.LocalDateTime

data class SleepLog(
    val userId: Int,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val totalTime: Long,
    val userFeel: Int
)

enum class UserFeel(val databaseValue: Int) {
    BAD(1),
    OK(2),
    GOOD(3)
}
