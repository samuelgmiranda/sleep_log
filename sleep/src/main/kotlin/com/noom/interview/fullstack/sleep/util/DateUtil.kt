package com.noom.interview.fullstack.sleep.util

import java.time.Duration
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

    fun historyStartDate(endDate: LocalDate, historyDays: Int): LocalDate =
        endDate.minusDays(historyDays.toLong() - 1)

    fun historyEndDate(currentDate: LocalDate): LocalDate =
        currentDate.minusDays(1)

    fun currentLocalDate(): LocalDate = LocalDate.now(clock)

    fun minutesAfterMidnight(dateTime: LocalDateTime): Int =
        ((dateTime.toLocalTime().toSecondOfDay() + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE) % MINUTES_PER_DAY

    fun normalizeMinutesAcrossMidnight(minutes: List<Int>): List<Int> {
        if (minutes.size < 2) return minutes

        val sorted = minutes.sorted()
        val largestGap = sorted.indices.maxOf { index ->
            val next = sorted[(index + 1) % sorted.size]
            (next - sorted[index] + MINUTES_PER_DAY) % MINUTES_PER_DAY
        }
        val periodStart = sorted.indices
            .filter { index ->
                val next = sorted[(index + 1) % sorted.size]
                (next - sorted[index] + MINUTES_PER_DAY) % MINUTES_PER_DAY == largestGap
            }
            .map { index -> sorted[(index + 1) % sorted.size] }
            .minByOrNull { start -> midnightDistance(averageWithPeriodStart(minutes, start)) }!!

        return normalizedMinutes(minutes, periodStart)
    }

    fun ceilingAverageMinute(minutes: List<Int>): Int =
        kotlin.math.ceil(minutes.average()).toInt() % MINUTES_PER_DAY

    fun formatClockTime(minutes: Int): String =
        LocalTime.ofSecondOfDay((minutes % MINUTES_PER_DAY) * 60L)
            .format(TIME_FORMATTER)
            .lowercase()

    fun formatDate(date: LocalDate, formatter: DateTimeFormatter = MONTH_FORMATTER): String =
        "${date.format(formatter)} ${date.dayOfMonth}${ordinalSuffix(date.dayOfMonth)}"

    private fun ordinalSuffix(dayOfMonth: Int): String =
        when {
            dayOfMonth in 11..13 -> "th"
            dayOfMonth % 10 == 1 -> "st"
            dayOfMonth % 10 == 2 -> "nd"
            dayOfMonth % 10 == 3 -> "rd"
            else -> "th"
        }

    private fun averageWithPeriodStart(minutes: List<Int>, periodStart: Int): Int =
        ceilingAverageMinute(normalizedMinutes(minutes, periodStart))

    private fun normalizedMinutes(minutes: List<Int>, periodStart: Int): List<Int> =
        minutes.map { minute -> if (minute < periodStart) minute + MINUTES_PER_DAY else minute }

    private fun midnightDistance(minutes: Int): Int = minOf(minutes, MINUTES_PER_DAY - minutes)

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

    val MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)
    val SHORT_MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
    private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    private const val MINUTES_PER_DAY = 24 * 60
    private const val SECONDS_PER_MINUTE = 60
    private val clock: Clock = Clock.systemDefaultZone()
}
