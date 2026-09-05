package com.noom.interview.fullstack.sleep.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

object DateUtil {

    fun parseDate(value: String): LocalDateTime =
        LocalDateTime.parse(value, DATE_TIME_FORMATTER)

    fun parseLocalDate(value: String): LocalDate =
        LocalDate.parse(value, LOCAL_DATE_FORMATTER)

    fun minutesBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long =
        Duration.between(startDate, endDate).toMinutes()

    fun sleepDate(startDate: LocalDateTime): LocalDate =
        startDate.toLocalDate()

    fun startOfDay(date: LocalDate): LocalDateTime =
        date.atStartOfDay()

    fun startOfNextDay(date: LocalDate): LocalDateTime =
        date.plusDays(1).atStartOfDay()

    fun formatDate(date: LocalDate): String {
        val suffix = when {
            date.dayOfMonth in 11..13 -> "th"
            date.dayOfMonth % 10 == 1 -> "st"
            date.dayOfMonth % 10 == 2 -> "nd"
            date.dayOfMonth % 10 == 3 -> "rd"
            else -> "th"
        }
        return "${date.format(MONTH_FORMATTER)} ${date.dayOfMonth}$suffix"
    }

    fun formatDuration(totalMinutes: Long): String =
        "%02d:%02d".format(totalMinutes / 60, totalMinutes % 60)

    fun formatTime(dateTime: LocalDateTime): String =
        dateTime.format(TIME_FORMATTER).lowercase()

    private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
        .ofPattern("MM/dd/uuuu HH:mm")
        .withResolverStyle(ResolverStyle.STRICT)

    private val LOCAL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
        .ofPattern("MM/dd/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    private val MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)
    private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
}
