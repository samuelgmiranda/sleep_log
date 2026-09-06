package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dao.SleepLogDAO
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.util.DateUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class SleepLogHistoryTest {

    private val dao = mock<SleepLogDAO>()
    private val service = SleepLogService(dao, SleepHistoryDTOBuilder())

    @Test
    fun queriesTheInclusiveMaximumHistoryRangeAndAggregatesEveryMatchingLog() {
        whenever(dao.findAllByUserAndStartDateBetween(eq(2), any(), any())).thenReturn(
            listOf(
                sleepLog(4, "2026-01-01T22:00", "2026-01-02T05:00", 420, 2),
                sleepLog(5, "2026-01-05T22:00", "2026-01-06T05:00", 420, 3),
                sleepLog(6, "2026-01-08T00:00", "2026-01-09T05:00", 1740, 2),
                sleepLog(7, "2026-09-04T23:00", "2026-09-05T08:00", 540, 3)
            )
        )

        val response = service.getSleepHistory(2, 365)
        val startCaptor = argumentCaptor<LocalDateTime>()
        val endCaptor = argumentCaptor<LocalDateTime>()
        verify(dao).findAllByUserAndStartDateBetween(eq(2), startCaptor.capture(), endCaptor.capture())

        val historyEndDate = DateUtil.historyEndDate(DateUtil.currentLocalDate())
        assertEquals(historyEndDate.minusDays(364).atStartOfDay(), startCaptor.firstValue)
        assertEquals(historyEndDate.plusDays(1).atStartOfDay(), endCaptor.firstValue)
        assertEquals(DateUtil.formatDate(historyEndDate.minusDays(364), DateUtil.SHORT_MONTH_FORMATTER), response.dateRangeStart)
        assertEquals(DateUtil.formatDate(DateUtil.currentLocalDate(), DateUtil.SHORT_MONTH_FORMATTER), response.dateRangeEnd)
        assertEquals("13:00", response.averageDuration)
        assertEquals("10:45 pm", response.averageStart)
        assertEquals("05:45 am", response.averageEnd)
        assertEquals(0, response.userFeelTotals["BAD"])
        assertEquals(2, response.userFeelTotals["OK"])
        assertEquals(2, response.userFeelTotals["GOOD"])
    }

    private fun sleepLog(id: Int, startDate: String, endDate: String, totalTime: Long, userFeel: Int) = SleepLog(
        2, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate), totalTime, userFeel, id
    )
}
