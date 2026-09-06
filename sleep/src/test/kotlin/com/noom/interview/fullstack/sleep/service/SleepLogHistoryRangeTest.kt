package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dao.SleepLogDAO
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.util.DateUtil
import com.noom.interview.fullstack.sleep.validation.SleepHistoryValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

class SleepLogHistoryRangeTest {

    private val dao = mock<SleepLogDAO>()
    private val service = SleepLogService(dao, SleepHistoryDTOBuilder())

    @Test
    fun queriesOneDayRangeForThePreviousCompletedLocalDate() {
        val endDate = currentHistoryEndDate()
        assertRange(1, endDate, endDate.plusDays(1))
    }

    @Test
    fun queriesDefaultThirtyDayRangeFromTheCurrentLocalDate() {
        assertEquals(30, SleepHistoryValidator().validateHistoryDays(null))
        val endDate = currentHistoryEndDate()
        assertRange(30, endDate.minusDays(29), endDate.plusDays(1))
    }

    @Test
    fun queriesTwoCompletedDaysWithoutIncludingAStartAtCurrentDayMidnight() {
        val endDate = currentHistoryEndDate()
        assertRange(2, endDate.minusDays(1), endDate.plusDays(1))
    }

    @Test
    fun queriesMaximumThreeHundredSixtyFiveDayRangeFromTheCurrentLocalDate() {
        val endDate = currentHistoryEndDate()
        assertRange(365, endDate.minusDays(364), endDate.plusDays(1))
    }

    @Test
    fun includesPreviousDayLogThatEndsOnTheCurrentDateInOneDayHistory() {
        val previousDayLog = SleepLog(2, currentHistoryEndDate().atTime(23, 0), currentHistoryEndDate().plusDays(1).atTime(8, 0), 540, 3, 7)
        whenever(dao.findAllByUserAndStartDateBetween(eq(2), any(), any()))
            .thenReturn(listOf(previousDayLog))

        val response = service.getSleepHistory(2, 1)
        val startCaptor = argumentCaptor<LocalDateTime>()
        val endCaptor = argumentCaptor<LocalDateTime>()
        verify(dao).findAllByUserAndStartDateBetween(eq(2), startCaptor.capture(), endCaptor.capture())
        assertEquals(currentHistoryEndDate().atStartOfDay(), startCaptor.firstValue)
        assertEquals(currentHistoryEndDate().plusDays(1).atStartOfDay(), endCaptor.firstValue)
        assertEquals(DateUtil.formatDate(currentHistoryEndDate(), DateUtil.SHORT_MONTH_FORMATTER), response.dateRangeStart)
        assertEquals(DateUtil.formatDate(currentHistoryEndDate().plusDays(1), DateUtil.SHORT_MONTH_FORMATTER), response.dateRangeEnd)
        assertEquals("09:00", response.averageDuration)
        assertEquals("11:00 pm", response.averageStart)
        assertEquals("08:00 am", response.averageEnd)
        assertEquals(0, response.userFeelTotals["BAD"])
        assertEquals(0, response.userFeelTotals["OK"])
        assertEquals(1, response.userFeelTotals["GOOD"])
    }

    @Test
    fun includesPreviousDayLogStartingAtElevenFiftyNinePmInOneDayHistory() {
        val previousCompletedDate = currentHistoryEndDate()
        val lastMinuteLog = SleepLog(2, previousCompletedDate.atTime(23, 59), previousCompletedDate.plusDays(1).atTime(7, 59), 480, 2, 8)
        whenever(dao.findAllByUserAndStartDateBetween(eq(2), any(), any()))
            .thenReturn(listOf(lastMinuteLog))

        val response = service.getSleepHistory(2, 1)
        val startCaptor = argumentCaptor<LocalDateTime>()
        val endCaptor = argumentCaptor<LocalDateTime>()
        verify(dao).findAllByUserAndStartDateBetween(eq(2), startCaptor.capture(), endCaptor.capture())
        assertEquals(previousCompletedDate.atStartOfDay(), startCaptor.firstValue)
        assertEquals(previousCompletedDate.plusDays(1).atStartOfDay(), endCaptor.firstValue)
        assertEquals(DateUtil.formatDate(previousCompletedDate, DateUtil.SHORT_MONTH_FORMATTER), response.dateRangeStart)
        assertEquals(DateUtil.formatDate(previousCompletedDate.plusDays(1), DateUtil.SHORT_MONTH_FORMATTER), response.dateRangeEnd)
        assertEquals("08:00", response.averageDuration)
        assertEquals("11:59 pm", response.averageStart)
        assertEquals("07:59 am", response.averageEnd)
        assertEquals(0, response.userFeelTotals["BAD"])
        assertEquals(1, response.userFeelTotals["OK"])
        assertEquals(0, response.userFeelTotals["GOOD"])
    }

    private fun assertRange(historyDays: Int, expectedStart: LocalDate, expectedEndExclusive: LocalDate) {
        whenever(dao.findAllByUserAndStartDateBetween(eq(2), any(), any()))
            .thenReturn(listOf(SleepLog(2, expectedStart.atTime(22, 0), expectedStart.plusDays(1).atTime(6, 0), 480, 2, 1)))

        service.getSleepHistory(2, historyDays)

        val startCaptor = argumentCaptor<LocalDateTime>()
        val endCaptor = argumentCaptor<LocalDateTime>()
        verify(dao).findAllByUserAndStartDateBetween(eq(2), startCaptor.capture(), endCaptor.capture())
        assertEquals(expectedStart.atStartOfDay(), startCaptor.firstValue)
        assertEquals(expectedEndExclusive.atStartOfDay(), endCaptor.firstValue)
    }

    private fun currentHistoryEndDate() = DateUtil.historyEndDate(DateUtil.currentLocalDate())
}
