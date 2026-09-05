package com.noom.interview.fullstack.sleep.model

data class SleepLogDTO(
    val id: Int,
    val targetDate: String,
    val sleepDuration: String,
    val startSleep: String,
    val endSleep: String,
    val userFeel: String
)
