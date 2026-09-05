package com.noom.interview.fullstack.sleep.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object DateUtil {

    fun parseDate(value: String): LocalDateTime =
        LocalDateTime.parse(value, DATE_TIME_FORMATTER)

    fun minutesBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long =
        Duration.between(startDate, endDate).toMinutes()

    fun sleepDate(startDate: LocalDateTime): LocalDate =
        startDate.toLocalDate()

    fun startOfDay(date: LocalDate): LocalDateTime =
        date.atStartOfDay()

    fun startOfNextDay(date: LocalDate): LocalDateTime =
        date.plusDays(1).atStartOfDay()

    private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
        .ofPattern("MM/dd/uuuu HH:mm")
        .withResolverStyle(ResolverStyle.STRICT)
}