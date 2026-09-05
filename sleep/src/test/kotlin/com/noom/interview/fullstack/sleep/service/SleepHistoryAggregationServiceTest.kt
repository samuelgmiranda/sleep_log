package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.model.SleepLog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SleepHistoryAggregationServiceTest {

    private val aggregationService = SleepHistoryAggregationService()

    @Test
    fun shouldAggregateTimesSymmetricallyAcrossMidnight() {
        val sleepLogs = SleepHistoryTestData.logs(
            LocalDate.of(2026, 11, 1),
            listOf("21:30" to "04:30", "22:30" to "05:30", "23:30" to "06:30", "00:30" to "07:30", "01:30" to "08:30", "02:30" to "09:30")
        )

        assertAggregation(sleepLogs, "12:00 am", "07:00 am", "07:00", 420L)
        assertEquals(LocalDate.of(2026, 11, 1), sleepLogs.first().startDate.toLocalDate())
        assertEquals(LocalDate.of(2026, 11, 30), sleepLogs.last().endDate.toLocalDate())
    }

    @Test
    fun shouldAggregateTimesImmediatelyAroundMidnight() {
        val sleepLogs = SleepHistoryTestData.logs(
            LocalDate.of(2026, 5, 1),
            listOf("23:45" to "07:45", "23:50" to "07:50", "23:55" to "07:55", "00:05" to "08:05", "00:10" to "08:10", "00:15" to "08:15")
        )

        assertAggregation(sleepLogs, "12:00 am", "08:00 am", "08:00", 480L)
        assertEquals(LocalDate.of(2026, 5, 30), sleepLogs.last().startDate.toLocalDate())
        assertEquals(LocalDate.of(2026, 5, 30), sleepLogs.last().endDate.toLocalDate())
    }

    @Test
    fun shouldAggregateDifferentDurationsAcrossMonthBoundary() {
        val sleepLogs = SleepHistoryTestData.logs(
            LocalDate.of(2026, 1, 27),
            listOf("21:00" to "03:30", "21:30" to "04:30", "22:00" to "05:30", "22:30" to "06:30", "23:00" to "07:30", "23:30" to "08:30")
        )

        assertAggregation(sleepLogs, "10:15 pm", "06:00 am", "07:45", 465L)
        assertEquals(LocalDate.of(2026, 2, 1), sleepLogs[4].endDate.toLocalDate())
        assertEquals(List(5) { listOf(390L, 420L, 450L, 480L, 510L, 540L) }.flatten(), durations(sleepLogs))
    }

    @Test
    fun shouldAggregateCrossMidnightTimesAcrossYearBoundary() {
        val sleepLogs = SleepHistoryTestData.logs(
            LocalDate.of(2026, 12, 27),
            listOf("22:15" to "05:45", "22:45" to "06:27", "23:15" to "07:09", "23:45" to "07:51", "00:15" to "08:33", "00:45" to "09:15")
        )

        assertAggregation(sleepLogs, "11:30 pm", "07:30 am", "08:00", 480L)
        assertEquals(LocalDate.of(2027, 1, 1), sleepLogs[5].startDate.toLocalDate())
        assertEquals(LocalDate.of(2027, 1, 3), sleepLogs[6].endDate.toLocalDate())
        assertEquals(List(5) { listOf(450L, 462L, 474L, 486L, 498L, 510L) }.flatten(), durations(sleepLogs))
    }

    @Test
    fun shouldAggregateWideCrossMidnightRangeAcrossLeapDay() {
        val sleepLogs = SleepHistoryTestData.logs(
            LocalDate.of(2028, 2, 25),
            listOf("20:30" to "02:30", "22:00" to "05:00", "23:30" to "07:30", "00:30" to "09:30", "02:00" to "12:00", "03:30" to "14:30")
        )

        assertAggregation(sleepLogs, "12:00 am", "08:30 am", "08:30", 510L)
        assertEquals(LocalDate.of(2028, 2, 29), sleepLogs[4].startDate.toLocalDate())
        assertEquals(LocalDate.of(2028, 3, 1), sleepLogs[5].startDate.toLocalDate())
        assertEquals(List(5) { listOf(360L, 420L, 480L, 540L, 600L, 660L) }.flatten(), durations(sleepLogs))
    }

    @Test
    fun shouldPreferMidnightWhenCircularTimeGapsTie() {
        val sleepLogs = listOf(
            sleepLog("2026-01-01T06:00", "2026-01-01T07:00"),
            sleepLog("2026-01-02T18:00", "2026-01-02T19:00")
        )

        assertEquals("12:00 am", aggregationService.averageBedTime(sleepLogs))
    }

    @Test
    fun shouldCeilingTimestampSecondsBeforeClockAggregation() {
        val sleepLogs = listOf(
            sleepLog("2026-01-01T23:59:59", "2026-01-02T00:00:01"),
            sleepLog("2026-01-02T00:00:01", "2026-01-02T23:59:59")
        )

        assertEquals("12:01 am", aggregationService.averageBedTime(sleepLogs))
        assertEquals("12:01 am", aggregationService.averageWakeTime(sleepLogs))
    }

    @Test
    fun shouldCeilingFractionalMinuteAcrossMidnight() {
        val sleepLogs = listOf(
            sleepLog("2026-01-01T23:59", "2026-01-02T07:00"),
            sleepLog("2026-01-02T00:00", "2026-01-02T07:00"),
            sleepLog("2026-01-03T00:00", "2026-01-03T07:00")
        )

        assertEquals("12:00 am", aggregationService.averageBedTime(sleepLogs))
    }

    @Test
    fun shouldWeightCrossMidnightOutliersByTheirFrequency() {
        val mostlyLate = List(29) { index -> sleepLog("2026-02-01T23:00", "2026-02-02T07:00", index) } + sleepLog("2026-03-01T01:00", "2026-03-01T09:00", 30)
        val mostlyEarly = List(29) { index -> sleepLog("2026-02-01T01:00", "2026-02-01T09:00", index) } + sleepLog("2026-03-01T23:00", "2026-03-02T07:00", 30)

        assertEquals("11:04 pm", aggregationService.averageBedTime(mostlyLate))
        assertEquals("12:56 am", aggregationService.averageBedTime(mostlyEarly))
    }

    private fun assertAggregation(
        sleepLogs: List<SleepLog>,
        expectedStart: String,
        expectedEnd: String,
        expectedDuration: String,
        expectedDurationMinutes: Long
    ) {
        assertEquals(expectedStart, aggregationService.averageBedTime(sleepLogs))
        assertEquals(expectedEnd, aggregationService.averageWakeTime(sleepLogs))
        assertEquals(expectedDuration, aggregationService.averageDuration(sleepLogs))
        assertEquals(expectedDurationMinutes, durations(sleepLogs).average().toLong())
    }

    private fun durations(sleepLogs: List<SleepLog>): List<Long> =
        sleepLogs.map { Duration.between(it.startDate, it.endDate).toMinutes() }

    private fun sleepLog(startDate: String, endDate: String, id: Int = 1): SleepLog {
        val start = LocalDateTime.parse(startDate)
        val end = LocalDateTime.parse(endDate)
        return SleepLog(2, start, end, Duration.between(start, end).toMinutes(), 2, id)
    }
}

internal object SleepHistoryTestData {

    fun logs(firstDate: LocalDate, timePairs: List<Pair<String, String>>): List<SleepLog> =
        List(30) { index ->
            val (startTime, endTime) = timePairs[index % timePairs.size]
            val startDateTime = LocalDateTime.of(firstDate.plusDays(index.toLong()), LocalTime.parse(startTime))
            val endDateTime = LocalDateTime.of(
                if (LocalTime.parse(endTime) <= LocalTime.parse(startTime)) firstDate.plusDays(index.toLong() + 1) else firstDate.plusDays(index.toLong()),
                LocalTime.parse(endTime)
            )
            SleepLog(
                userId = 2,
                startDate = startDateTime,
                endDate = endDateTime,
                totalTime = Duration.between(startDateTime, endDateTime).toMinutes(),
                userFeel = index % 3 + 1,
                id = index + 1
            )
        }
}
