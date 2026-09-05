package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.controller.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.dao.SleepLogDAO;
import com.noom.interview.fullstack.sleep.exception.BusinessValidationException;
import com.noom.interview.fullstack.sleep.exception.ResourceNotFoundException;
import com.noom.interview.fullstack.sleep.model.SleepLog;
import com.noom.interview.fullstack.sleep.model.SleepLogDTO;
import com.noom.interview.fullstack.sleep.model.SleepHistoryDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SleepLogServiceTest {

    private final SleepLogDAO dao = org.mockito.Mockito.mock(SleepLogDAO.class);
    private final SleepLogService service = new SleepLogService(dao);

    @Test
    public void savesUserScopedLogWithDurationAndFeeling() {
        when(dao.existsForUserAndSleepDate(7, LocalDate.of(2026, 9, 3))).thenReturn(false);

        service.createSleepLog(7, new CreateSleepLogRequest(
                "09/03/2026 22:00", "09/04/2026 05:30", "GOOD"));

        ArgumentCaptor<SleepLog> captor = ArgumentCaptor.forClass(SleepLog.class);
        verify(dao).save(captor.capture());
        SleepLog saved = captor.getValue();
        assertEquals(7, saved.getUserId());
        assertEquals(450L, saved.getTotalTime());
        assertEquals(3, saved.getUserFeel());
    }

    @Test
    public void rejectsDuplicateSleepDateBeforeSaving() {
        when(dao.existsForUserAndSleepDate(7, LocalDate.of(2026, 9, 3))).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> service.createSleepLog(7,
                new CreateSleepLogRequest("09/03/2026 22:00", "09/04/2026 05:30", "OK")));

        verify(dao, never()).save(org.mockito.ArgumentMatchers.any(SleepLog.class));
    }

    @Test
    public void composesFormattedResponseForAllOrdinalSuffixesAndFeelingValues() {
        int[] days = {1, 2, 3, 4, 10, 11, 12, 13, 20, 21, 22, 23, 31};
        String[] suffixes = {"st", "nd", "rd", "th", "th", "th", "th", "th", "th", "st", "nd", "rd", "st"};
        int[] feelings = {1, 2, 3};
        String[] feelingNames = {"BAD", "OK", "GOOD"};

        for (int i = 0; i < days.length; i++) {
            int feeling = feelings[i % feelings.length];
            java.time.LocalDateTime start = java.time.LocalDateTime.of(2026, 10, days[i], 23, 0);
            SleepLog log = new SleepLog(7, start, start.plusHours(9), 540, feeling, days[i]);
            when(dao.findByUserAndStartDateBetween(org.mockito.ArgumentMatchers.eq(7),
                    org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class),
                    org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class))).thenReturn(log);

            SleepLogDTO response = service.getSleepLog(7, java.time.LocalDate.of(2026, 10, days[i]));

            assertEquals("October " + days[i] + suffixes[i], response.getTargetDate());
            assertEquals("09:00", response.getSleepDuration());
            assertEquals("11:00 pm", response.getStartSleep());
            assertEquals("08:00 am", response.getEndSleep());
            assertEquals(feelingNames[i % feelings.length], response.getUserFeel());
        }
    }

    @Test
    public void usesPreviousLocalDateWhenTargetDateIsOmitted() {
        when(dao.findByUserAndStartDateBetween(org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class)))
                .thenReturn(new SleepLog(7, java.time.LocalDateTime.now().minusDays(1).withHour(23),
                        java.time.LocalDateTime.now().withHour(8), 540, 3, 8));

        SleepLogDTO response = service.getSleepLog(7, null);

        assertEquals(com.noom.interview.fullstack.sleep.util.DateUtil.INSTANCE.formatDate(
                java.time.LocalDate.now().minusDays(1),
                com.noom.interview.fullstack.sleep.util.DateUtil.INSTANCE.getMONTH_FORMATTER()), response.getTargetDate());
    }

    @Test
    public void returnsNotFoundWhenPreviousLocalDateHasNoSleepLog() {
        when(dao.findByUserAndStartDateBetween(org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class)))
                .thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.getSleepLog(7, null));
    }

    @Test
    public void aggregatesTimesSymmetricallyAcrossMidnightThroughTheService() {
        List<SleepLog> logs = historyLogs(LocalDate.of(2026, 11, 1), new String[][]{
                {"21:30", "04:30"}, {"22:30", "05:30"}, {"23:30", "06:30"},
                {"00:30", "07:30"}, {"01:30", "08:30"}, {"02:30", "09:30"}
        });

        assertHistoryAggregation(logs, "12:00 am", "07:00 am", "07:00", 420L);
        assertEquals(LocalDate.of(2026, 11, 1), logs.get(0).getStartDate().toLocalDate());
        assertEquals(LocalDate.of(2026, 11, 30), logs.get(29).getEndDate().toLocalDate());
        assertTrue(logs.stream().allMatch(log -> Duration.between(log.getStartDate(), log.getEndDate()).toMinutes() == 420L));
    }

    @Test
    public void aggregatesTimesImmediatelyAroundMidnightThroughTheService() {
        List<SleepLog> logs = historyLogs(LocalDate.of(2026, 5, 1), new String[][]{
                {"23:45", "07:45"}, {"23:50", "07:50"}, {"23:55", "07:55"},
                {"00:05", "08:05"}, {"00:10", "08:10"}, {"00:15", "08:15"}
        });

        assertHistoryAggregation(logs, "12:00 am", "08:00 am", "08:00", 480L);
        assertEquals(LocalDate.of(2026, 5, 30), logs.get(29).getStartDate().toLocalDate());
        assertTrue(logs.stream().allMatch(log -> Duration.between(log.getStartDate(), log.getEndDate()).toMinutes() == 480L));
    }

    @Test
    public void aggregatesDifferentDurationsAcrossAMonthBoundaryThroughTheService() {
        List<SleepLog> logs = historyLogs(LocalDate.of(2026, 1, 27), new String[][]{
                {"21:00", "03:30"}, {"21:30", "04:30"}, {"22:00", "05:30"},
                {"22:30", "06:30"}, {"23:00", "07:30"}, {"23:30", "08:30"}
        });

        assertHistoryAggregation(logs, "10:15 pm", "06:00 am", "07:45", 465L);
        assertEquals(LocalDate.of(2026, 2, 1), logs.get(4).getEndDate().toLocalDate());
        assertEquals(repeatedDurations(390L, 420L, 450L, 480L, 510L, 540L), durations(logs));
    }

    @Test
    public void aggregatesCrossMidnightTimesAcrossAYearBoundaryThroughTheService() {
        List<SleepLog> logs = historyLogs(LocalDate.of(2026, 12, 27), new String[][]{
                {"22:15", "05:45"}, {"22:45", "06:27"}, {"23:15", "07:09"},
                {"23:45", "07:51"}, {"00:15", "08:33"}, {"00:45", "09:15"}
        });

        assertHistoryAggregation(logs, "11:30 pm", "07:30 am", "08:00", 480L);
        assertEquals(LocalDate.of(2027, 1, 1), logs.get(5).getStartDate().toLocalDate());
        assertEquals(LocalDate.of(2027, 1, 3), logs.get(6).getEndDate().toLocalDate());
        assertEquals(repeatedDurations(450L, 462L, 474L, 486L, 498L, 510L), durations(logs));
    }

    @Test
    public void aggregatesWideCrossMidnightRangeAcrossALeapDayThroughTheService() {
        List<SleepLog> logs = historyLogs(LocalDate.of(2028, 2, 25), new String[][]{
                {"20:30", "02:30"}, {"22:00", "05:00"}, {"23:30", "07:30"},
                {"00:30", "09:30"}, {"02:00", "12:00"}, {"03:30", "14:30"}
        });

        assertHistoryAggregation(logs, "12:00 am", "08:30 am", "08:30", 510L);
        assertEquals(LocalDate.of(2028, 2, 29), logs.get(4).getStartDate().toLocalDate());
        assertEquals(LocalDate.of(2028, 3, 1), logs.get(5).getStartDate().toLocalDate());
        assertEquals(repeatedDurations(360L, 420L, 480L, 540L, 600L, 660L), durations(logs));
    }

    private void assertHistoryAggregation(
            List<SleepLog> logs,
            String expectedStart,
            String expectedEnd,
            String expectedDuration,
            long expectedDurationMinutes
    ) {
        when(dao.findAllByUserAndStartDateBetween(
                org.mockito.ArgumentMatchers.eq(2),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(logs);

        SleepHistoryDTO response = service.getSleepHistory(2, 4000);

        assertEquals(expectedStart, response.getAverageStart());
        assertEquals(expectedEnd, response.getAverageEnd());
        assertEquals(expectedDuration, response.getAverageDuration());
        assertEquals(expectedDurationMinutes, durations(logs).stream().mapToLong(Long::longValue).average().orElseThrow());
        assertEquals(10, response.getUserFeelTotals().get("BAD"));
        assertEquals(10, response.getUserFeelTotals().get("OK"));
        assertEquals(10, response.getUserFeelTotals().get("GOOD"));
    }

    private List<SleepLog> historyLogs(LocalDate firstDate, String[][] timePairs) {
        List<SleepLog> logs = new ArrayList<>();
        for (int index = 0; index < 30; index++) {
            String[] timePair = timePairs[index % timePairs.length];
            LocalTime startTime = LocalTime.parse(timePair[0]);
            LocalTime endTime = LocalTime.parse(timePair[1]);
            LocalDate startDate = firstDate.plusDays(index);
            LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(endTime.isAfter(startTime) ? startDate : startDate.plusDays(1), endTime);
            logs.add(new SleepLog(2, startDateTime, endDateTime,
                    Duration.between(startDateTime, endDateTime).toMinutes(), index % 3 + 1, index + 1));
        }
        return logs;
    }

    private List<Long> durations(List<SleepLog> logs) {
        return logs.stream()
                .map(log -> Duration.between(log.getStartDate(), log.getEndDate()).toMinutes())
                .collect(java.util.stream.Collectors.toList());
    }

    private List<Long> repeatedDurations(Long... values) {
        List<Long> durations = new ArrayList<>();
        for (int repeat = 0; repeat < 5; repeat++) {
            durations.addAll(Arrays.asList(values));
        }
        return durations;
    }
}
