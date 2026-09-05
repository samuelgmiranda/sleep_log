package com.noom.interview.fullstack.sleep.dao

import com.noom.interview.fullstack.sleep.model.SleepLog
import java.time.LocalDate

interface SleepLogDAO {
    fun existsForUserAndSleepDate(userId: Int, sleepDate: LocalDate): Boolean
    fun save(sleepLog: SleepLog)
}