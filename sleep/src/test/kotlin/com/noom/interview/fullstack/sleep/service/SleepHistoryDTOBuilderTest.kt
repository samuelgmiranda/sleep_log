package com.noom.interview.fullstack.sleep.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SleepHistoryDTOBuilderTest {

    @Test
    fun shouldBuildLeapYearHistoryWithAllAggregations() {
        val sleepLogs = SleepHistoryTestData.logs(
            LocalDate.of(2028, 2, 25),
            listOf("20:30" to "02:30", "22:00" to "05:00", "23:30" to "07:30", "00:30" to "09:30", "02:00" to "12:00", "03:30" to "14:30")
        )

        val history = SleepHistoryDTOBuilder(SleepHistoryAggregationService())
            .forUser(2)
            .addRangeStart(LocalDate.of(2028, 2, 25))
            .addRangeEnd(LocalDate.of(2028, 3, 25))
            .withAverageDuration(sleepLogs)
            .withAverageStart(sleepLogs)
            .withAverageEnd(sleepLogs)
            .withUserFeelTotals(sleepLogs)
            .build()

        assertEquals(2, history.userId)
        assertEquals("Feb 25th", history.dateRangeStart)
        assertEquals("Mar 25th", history.dateRangeEnd)
        assertEquals("08:30", history.averageDuration)
        assertEquals("12:00 am", history.averageStart)
        assertEquals("08:30 am", history.averageEnd)
        assertEquals(mapOf("BAD" to 10, "OK" to 10, "GOOD" to 10), history.userFeelTotals)
    }
}
