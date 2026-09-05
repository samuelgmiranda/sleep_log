package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.controller.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.dao.SleepLogDAO;
import com.noom.interview.fullstack.sleep.exception.BusinessValidationException;
import com.noom.interview.fullstack.sleep.model.SleepLog;
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
}
