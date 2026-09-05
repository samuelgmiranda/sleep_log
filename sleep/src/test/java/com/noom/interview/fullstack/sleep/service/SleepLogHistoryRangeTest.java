package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.dao.SleepLogDAO;
import com.noom.interview.fullstack.sleep.model.SleepLog;
import com.noom.interview.fullstack.sleep.validation.SleepHistoryValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SleepLogHistoryRangeTest {

    private final SleepLogDAO dao = mock(SleepLogDAO.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-05T12:00:00Z"), ZoneId.of("UTC"));
    private final SleepLogService service = new SleepLogService(dao, new SleepHistoryDTOBuilder(), clock);

    @Test
    void queriesOneDayRangeFromTheFixedCurrentDate() {
        assertRange(1, LocalDate.of(2026, 9, 5), LocalDate.of(2026, 9, 6));
    }

    @Test
    void queriesDefaultThirtyDayRangeFromTheFixedCurrentDate() {
        assertEquals(30, new SleepHistoryValidator().validateHistoryDays(null));
        assertRange(30, LocalDate.of(2026, 8, 7), LocalDate.of(2026, 9, 6));
    }

    @Test
    void queriesMaximumThreeHundredSixtyFiveDayRangeFromTheFixedCurrentDate() {
        assertRange(365, LocalDate.of(2025, 9, 6), LocalDate.of(2026, 9, 6));
    }

    private void assertRange(int historyDays, LocalDate expectedStart, LocalDate expectedEndExclusive) {
        when(dao.findAllByUserAndStartDateBetween(eq(2), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new SleepLog(2, expectedStart.atTime(22, 0), expectedStart.plusDays(1).atTime(6, 0), 480, 2, 1)));

        service.getSleepHistory(2, historyDays);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(dao).findAllByUserAndStartDateBetween(eq(2), startCaptor.capture(), endCaptor.capture());
        assertEquals(expectedStart.atStartOfDay(), startCaptor.getValue());
        assertEquals(expectedEndExclusive.atStartOfDay(), endCaptor.getValue());
    }
}
