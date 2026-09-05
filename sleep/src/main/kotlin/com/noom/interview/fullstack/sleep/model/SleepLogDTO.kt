package com.noom.interview.fullstack.sleep.model

data class SleepLogDTO(
    val id: Int,
    val targetDate: String,
    val sleepDuration: String,
    val startSleep: String,
    val endSleep: String,
    val userFeel: String
)

data class SleepHistoryDTO(
    val userId: Int,
    val dateRangeStart: String,
    val dateRangeEnd: String,
    val averageDuration: String,
    val averageStart: String,
    val averageEnd: String,
    val userFeelTotals: Map<String, Int>
)
