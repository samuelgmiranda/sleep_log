package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.model.SleepLog
import java.time.LocalDate

interface SleepLogRepository {
    fun existsForUserAndSleepDate(userId: Int, sleepDate: LocalDate): Boolean
    fun save(sleepLog: SleepLog)
}
