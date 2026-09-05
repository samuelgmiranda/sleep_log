package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.dao.SleepLogDAO;
import com.noom.interview.fullstack.sleep.model.SleepHistoryDTO;
import com.noom.interview.fullstack.sleep.model.SleepLog;
import com.noom.interview.fullstack.sleep.util.DateUtil;
import com.noom.interview.fullstack.sleep.validation.SleepHistoryValidator;
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

class SleepLogHistoryRangeTest {

    private final SleepLogDAO dao = mock(SleepLogDAO.class);
    private final SleepLogService service = new SleepLogService(dao, new SleepHistoryDTOBuilder());

    @Test
    void queriesOneDayRangeForThePreviousCompletedLocalDate() {
        LocalDate endDate = currentHistoryEndDate();
        assertRange(1, endDate, endDate.plusDays(1));
    }

    @Test
    void queriesDefaultThirtyDayRangeFromTheCurrentLocalDate() {
        assertEquals(30, new SleepHistoryValidator().validateHistoryDays(null));
        LocalDate endDate = currentHistoryEndDate();
        assertRange(30, endDate.minusDays(29), endDate.plusDays(1));
    }

    @Test
    void queriesTwoCompletedDaysWithoutIncludingAStartAtCurrentDayMidnight() {
        LocalDate endDate = currentHistoryEndDate();
        assertRange(2, endDate.minusDays(1), endDate.plusDays(1));
    }

    @Test
    void queriesMaximumThreeHundredSixtyFiveDayRangeFromTheCurrentLocalDate() {
        LocalDate endDate = currentHistoryEndDate();
        assertRange(365, endDate.minusDays(364), endDate.plusDays(1));
    }

    @Test
    void includesPreviousDayLogThatEndsOnTheCurrentDateInOneDayHistory() {
        SleepLog previousDayLog = new SleepLog(
                2,
                currentHistoryEndDate().atTime(23, 0),
                currentHistoryEndDate().plusDays(1).atTime(8, 0),
                540,
                3,
                7
        );
        when(dao.findAllByUserAndStartDateBetween(eq(2), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(previousDayLog));

        SleepHistoryDTO response = service.getSleepHistory(2, 1);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(dao).findAllByUserAndStartDateBetween(eq(2), startCaptor.capture(), endCaptor.capture());
        assertEquals(currentHistoryEndDate().atStartOfDay(), startCaptor.getValue());
        assertEquals(currentHistoryEndDate().plusDays(1).atStartOfDay(), endCaptor.getValue());
        assertEquals(DateUtil.INSTANCE.formatDate(currentHistoryEndDate(), DateUtil.INSTANCE.getSHORT_MONTH_FORMATTER()), response.getDateRangeStart());
        assertEquals(DateUtil.INSTANCE.formatDate(currentHistoryEndDate().plusDays(1), DateUtil.INSTANCE.getSHORT_MONTH_FORMATTER()), response.getDateRangeEnd());
        assertEquals("09:00", response.getAverageDuration());
        assertEquals("11:00 pm", response.getAverageStart());
        assertEquals("08:00 am", response.getAverageEnd());
        assertEquals(0, response.getUserFeelTotals().get("BAD"));
        assertEquals(0, response.getUserFeelTotals().get("OK"));
        assertEquals(1, response.getUserFeelTotals().get("GOOD"));
    }

    @Test
    void includesPreviousDayLogStartingAtElevenFiftyNinePmInOneDayHistory() {
        LocalDate previousCompletedDate = currentHistoryEndDate();
        SleepLog lastMinuteLog = new SleepLog(
                2,
                previousCompletedDate.atTime(23, 59),
                previousCompletedDate.plusDays(1).atTime(7, 59),
                480,
                2,
                8
        );
        when(dao.findAllByUserAndStartDateBetween(eq(2), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(lastMinuteLog));

        SleepHistoryDTO response = service.getSleepHistory(2, 1);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(dao).findAllByUserAndStartDateBetween(eq(2), startCaptor.capture(), endCaptor.capture());
        assertEquals(previousCompletedDate.atStartOfDay(), startCaptor.getValue());
        assertEquals(previousCompletedDate.plusDays(1).atStartOfDay(), endCaptor.getValue());
        assertEquals(DateUtil.INSTANCE.formatDate(previousCompletedDate, DateUtil.INSTANCE.getSHORT_MONTH_FORMATTER()), response.getDateRangeStart());
        assertEquals(DateUtil.INSTANCE.formatDate(previousCompletedDate.plusDays(1), DateUtil.INSTANCE.getSHORT_MONTH_FORMATTER()), response.getDateRangeEnd());
        assertEquals("08:00", response.getAverageDuration());
        assertEquals("11:59 pm", response.getAverageStart());
        assertEquals("07:59 am", response.getAverageEnd());
        assertEquals(0, response.getUserFeelTotals().get("BAD"));
        assertEquals(1, response.getUserFeelTotals().get("OK"));
        assertEquals(0, response.getUserFeelTotals().get("GOOD"));
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

    private LocalDate currentHistoryEndDate() {
        return DateUtil.INSTANCE.historyEndDate(DateUtil.INSTANCE.currentLocalDate());
    }
}
