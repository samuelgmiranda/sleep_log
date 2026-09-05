package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.controller.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.dao.SleepLogDAO;
import com.noom.interview.fullstack.sleep.exception.BusinessValidationException;
import com.noom.interview.fullstack.sleep.exception.ResourceNotFoundException;
import com.noom.interview.fullstack.sleep.model.SleepLog;
import com.noom.interview.fullstack.sleep.model.SleepLogDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
