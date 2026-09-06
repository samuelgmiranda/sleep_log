package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.controller.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dao.SleepLogDAO
import com.noom.interview.fullstack.sleep.exception.BusinessValidationException
import com.noom.interview.fullstack.sleep.exception.ResourceNotFoundException
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.util.DateUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SleepLogServiceTest {

    private val dao = mock<SleepLogDAO>()
    private val service = SleepLogService(dao)

    @Test
    fun savesUserScopedLogWithDurationAndFeeling() {
        whenever(dao.existsForUserAndSleepDate(7, LocalDate.of(2026, 9, 3))).thenReturn(false)

        service.createSleepLog(7, CreateSleepLogRequest("09/03/2026 22:00", "09/04/2026 05:30", "GOOD"))

        val captor = argumentCaptor<SleepLog>()
        verify(dao).save(captor.capture())
        val saved = captor.firstValue
        assertEquals(7, saved.userId)
        assertEquals(450L, saved.totalTime)
        assertEquals(3, saved.userFeel)
    }

    @Test
    fun rejectsDuplicateSleepDateBeforeSaving() {
        whenever(dao.existsForUserAndSleepDate(7, LocalDate.of(2026, 9, 3))).thenReturn(true)

        assertThrows(BusinessValidationException::class.java) {
            service.createSleepLog(7, CreateSleepLogRequest("09/03/2026 22:00", "09/04/2026 05:30", "OK"))
        }

        verify(dao, never()).save(any())
    }

    @Test
    fun preservesStartDateAndFullDurationForCrossMidnightInterval() {
        val sleepDate = LocalDate.of(2026, 11, 1)
        whenever(dao.existsForUserAndSleepDate(1, sleepDate)).thenReturn(false)

        service.createSleepLog(1, CreateSleepLogRequest("11/01/2026 21:00", "11/02/2026 07:00", "GOOD"))

        val sleepLogCaptor = argumentCaptor<SleepLog>()
        verify(dao).existsForUserAndSleepDate(1, sleepDate)
        verify(dao).save(sleepLogCaptor.capture())
        assertEquals(sleepDate.atTime(21, 0), sleepLogCaptor.firstValue.startDate)
        assertEquals(LocalDate.of(2026, 11, 2).atTime(7, 0), sleepLogCaptor.firstValue.endDate)
        assertEquals(600L, sleepLogCaptor.firstValue.totalTime)
    }

    @Test
    fun rejectsSecondCrossMidnightIntervalForSameUserAndStartDate() {
        whenever(dao.existsForUserAndSleepDate(1, LocalDate.of(2026, 11, 1))).thenReturn(true)

        assertThrows(BusinessValidationException::class.java) {
            service.createSleepLog(1, CreateSleepLogRequest("11/01/2026 22:00", "11/02/2026 05:00", "OK"))
        }

        verify(dao, never()).save(any())
    }

    @Test
    fun retrievesCrossMidnightLogUsingTheStartDateWindow() {
        val sleepDate = LocalDate.of(2026, 11, 1)
        whenever(dao.findByUserAndStartDateBetween(1, sleepDate.atStartOfDay(), sleepDate.plusDays(1).atStartOfDay()))
            .thenReturn(SleepLog(1, sleepDate.atTime(21, 0), sleepDate.plusDays(1).atTime(7, 0), 600, 3, 1))

        val response = service.getSleepLog(1, sleepDate)

        assertEquals("10:00", response.sleepDuration)
        assertEquals("09:00 pm", response.startSleep)
        assertEquals("07:00 am", response.endSleep)
    }

    @Test
    fun composesFormattedResponseForAllOrdinalSuffixesAndFeelingValues() {
        val days = listOf(1, 2, 3, 4, 10, 11, 12, 13, 20, 21, 22, 23, 31)
        val suffixes = listOf("st", "nd", "rd", "th", "th", "th", "th", "th", "th", "st", "nd", "rd", "st")
        val feelings = listOf(1, 2, 3)
        val feelingNames = listOf("BAD", "OK", "GOOD")

        days.forEachIndexed { index, day ->
            val feeling = feelings[index % feelings.size]
            val start = LocalDateTime.of(2026, 10, day, 23, 0)
            val log = SleepLog(7, start, start.plusHours(9), 540, feeling, day)
            whenever(dao.findByUserAndStartDateBetween(eq(7), any(), any())).thenReturn(log)

            val response = service.getSleepLog(7, LocalDate.of(2026, 10, day))

            assertEquals("October $day${suffixes[index]}", response.targetDate)
            assertEquals("09:00", response.sleepDuration)
            assertEquals("11:00 pm", response.startSleep)
            assertEquals("08:00 am", response.endSleep)
            assertEquals(feelingNames[index % feelings.size], response.userFeel)
        }
    }

    @Test
    fun usesPreviousLocalDateWhenTargetDateIsOmitted() {
        whenever(dao.findByUserAndStartDateBetween(eq(7), any(), any()))
            .thenReturn(SleepLog(7, LocalDateTime.now().minusDays(1).withHour(23), LocalDateTime.now().withHour(8), 540, 3, 8))

        val response = service.getSleepLog(7, null)

        assertEquals(DateUtil.formatDate(LocalDate.now().minusDays(1), DateUtil.MONTH_FORMATTER), response.targetDate)
    }

    @Test
    fun returnsNotFoundWhenPreviousLocalDateHasNoSleepLog() {
        whenever(dao.findByUserAndStartDateBetween(eq(7), any(), any())).thenReturn(null)

        assertThrows(ResourceNotFoundException::class.java) { service.getSleepLog(7, null) }
    }

    @Test
    fun aggregatesTimesSymmetricallyAcrossMidnightThroughTheService() {
        val logs = historyLogs(LocalDate.of(2026, 11, 1), listOf("21:30" to "04:30", "22:30" to "05:30", "23:30" to "06:30", "00:30" to "07:30", "01:30" to "08:30", "02:30" to "09:30"))

        assertHistoryAggregation(logs, "12:00 am", "07:00 am", "07:00", 420L)
        assertEquals(LocalDate.of(2026, 11, 1), logs.first().startDate.toLocalDate())
        assertEquals(LocalDate.of(2026, 11, 30), logs[29].endDate.toLocalDate())
        assertTrue(logs.all { Duration.between(it.startDate, it.endDate).toMinutes() == 420L })
    }

    @Test
    fun aggregatesTimesImmediatelyAroundMidnightThroughTheService() {
        val logs = historyLogs(LocalDate.of(2026, 5, 1), listOf("23:45" to "07:45", "23:50" to "07:50", "23:55" to "07:55", "00:05" to "08:05", "00:10" to "08:10", "00:15" to "08:15"))

        assertHistoryAggregation(logs, "12:00 am", "08:00 am", "08:00", 480L)
        assertEquals(LocalDate.of(2026, 5, 30), logs[29].startDate.toLocalDate())
        assertTrue(logs.all { Duration.between(it.startDate, it.endDate).toMinutes() == 480L })
    }

    @Test
    fun aggregatesDifferentDurationsAcrossAMonthBoundaryThroughTheService() {
        val logs = historyLogs(LocalDate.of(2026, 1, 27), listOf("21:00" to "03:30", "21:30" to "04:30", "22:00" to "05:30", "22:30" to "06:30", "23:00" to "07:30", "23:30" to "08:30"))

        assertHistoryAggregation(logs, "10:15 pm", "06:00 am", "07:45", 465L)
        assertEquals(LocalDate.of(2026, 2, 1), logs[4].endDate.toLocalDate())
        assertEquals(repeatedDurations(390L, 420L, 450L, 480L, 510L, 540L), durations(logs))
    }

    @Test
    fun aggregatesCrossMidnightTimesAcrossAYearBoundaryThroughTheService() {
        val logs = historyLogs(LocalDate.of(2026, 12, 27), listOf("22:15" to "05:45", "22:45" to "06:27", "23:15" to "07:09", "23:45" to "07:51", "00:15" to "08:33", "00:45" to "09:15"))

        assertHistoryAggregation(logs, "11:30 pm", "07:30 am", "08:00", 480L)
        assertEquals(LocalDate.of(2027, 1, 1), logs[5].startDate.toLocalDate())
        assertEquals(LocalDate.of(2027, 1, 3), logs[6].endDate.toLocalDate())
        assertEquals(repeatedDurations(450L, 462L, 474L, 486L, 498L, 510L), durations(logs))
    }

    @Test
    fun aggregatesWideCrossMidnightRangeAcrossALeapDayThroughTheService() {
        val logs = historyLogs(LocalDate.of(2028, 2, 25), listOf("20:30" to "02:30", "22:00" to "05:00", "23:30" to "07:30", "00:30" to "09:30", "02:00" to "12:00", "03:30" to "14:30"))

        assertHistoryAggregation(logs, "12:00 am", "08:30 am", "08:30", 510L)
        assertEquals(LocalDate.of(2028, 2, 29), logs[4].startDate.toLocalDate())
        assertEquals(LocalDate.of(2028, 3, 1), logs[5].startDate.toLocalDate())
        assertEquals(repeatedDurations(360L, 420L, 480L, 540L, 600L, 660L), durations(logs))
    }

    private fun assertHistoryAggregation(logs: List<SleepLog>, expectedStart: String, expectedEnd: String, expectedDuration: String, expectedDurationMinutes: Long) {
        whenever(dao.findAllByUserAndStartDateBetween(eq(2), any(), any())).thenReturn(logs)

        val response = service.getSleepHistory(2, 365)

        assertEquals(expectedStart, response.averageStart)
        assertEquals(expectedEnd, response.averageEnd)
        assertEquals(expectedDuration, response.averageDuration)
        assertEquals(expectedDurationMinutes.toDouble(), durations(logs).average())
        assertEquals(10, response.userFeelTotals["BAD"])
        assertEquals(10, response.userFeelTotals["OK"])
        assertEquals(10, response.userFeelTotals["GOOD"])
    }

    private fun historyLogs(firstDate: LocalDate, timePairs: List<Pair<String, String>>) = List(30) { index ->
        val (start, end) = timePairs[index % timePairs.size]
        val startTime = LocalTime.parse(start)
        val endTime = LocalTime.parse(end)
        val startDate = firstDate.plusDays(index.toLong())
        val startDateTime = LocalDateTime.of(startDate, startTime)
        val endDateTime = LocalDateTime.of(if (endTime > startTime) startDate else startDate.plusDays(1), endTime)
        SleepLog(2, startDateTime, endDateTime, Duration.between(startDateTime, endDateTime).toMinutes(), index % 3 + 1, index + 1)
    }

    private fun durations(logs: List<SleepLog>) = logs.map { Duration.between(it.startDate, it.endDate).toMinutes() }

    private fun repeatedDurations(vararg values: Long) = List(5) { values.toList() }.flatten()
}
