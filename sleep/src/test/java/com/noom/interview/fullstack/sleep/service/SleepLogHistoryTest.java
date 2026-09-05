package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.dao.SleepLogDAO;
import com.noom.interview.fullstack.sleep.model.SleepHistoryDTO;
import com.noom.interview.fullstack.sleep.model.SleepLog;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SleepLogHistoryTest {

    private final SleepLogDAO dao = mock(SleepLogDAO.class);
    private final SleepLogService service = new SleepLogService(dao);

    @Test
    void queriesTheInclusiveMaximumHistoryRangeAndAggregatesEveryMatchingLog() {
        when(dao.findAllByUserAndStartDateBetween(eq(2), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        sleepLog(4, "2026-01-01T22:00", "2026-01-02T05:00", 420, 2),
                        sleepLog(5, "2026-01-05T22:00", "2026-01-06T05:00", 420, 3),
                        sleepLog(6, "2026-01-08T00:00", "2026-01-09T05:00", 1740, 2),
                        sleepLog(7, "2026-09-04T23:00", "2026-09-05T08:00", 540, 3)
                ));

        SleepHistoryDTO response = service.getSleepHistory(2, 365);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(dao).findAllByUserAndStartDateBetween(eq(2), startCaptor.capture(), endCaptor.capture());

        LocalDate endDate = LocalDate.now();
        assertEquals(endDate.minusDays(364).atStartOfDay(), startCaptor.getValue());
        assertEquals(endDate.plusDays(1).atStartOfDay(), endCaptor.getValue());
        assertEquals("13:00", response.getAverageDuration());
        assertEquals("10:45 pm", response.getAverageStart());
        assertEquals("05:45 am", response.getAverageEnd());
        assertEquals(0, response.getUserFeelTotals().get("BAD"));
        assertEquals(2, response.getUserFeelTotals().get("OK"));
        assertEquals(2, response.getUserFeelTotals().get("GOOD"));
    }

    private SleepLog sleepLog(int id, String startDate, String endDate, long totalTime, int userFeel) {
        return new SleepLog(
                2,
                LocalDateTime.parse(startDate),
                LocalDateTime.parse(endDate),
                totalTime,
                userFeel,
                id
        );
    }
}
