package com.noom.interview.fullstack.sleep.validation;

import com.noom.interview.fullstack.sleep.controller.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SleepLogValidatorTest {

    private final SleepLogValidator validator = new SleepLogValidator();

    @Test
    public void acceptsValidCrossMidnightRequest() {
        validator.validateCreateRequest(new CreateSleepLogRequest(
                "09/03/2026 22:00", "09/04/2026 05:30", "GOOD"));
    }

    @Test
    public void rejectsInvalidDate() {
        assertThrows(InvalidRequestException.class, () -> validator.validateCreateRequest(
                new CreateSleepLogRequest("09/31/2026 22:00", "10/01/2026 05:30", "GOOD")));
    }

    @Test
    public void rejectsInvalidFeeling() {
        assertThrows(InvalidRequestException.class, () -> validator.validateCreateRequest(
                new CreateSleepLogRequest("09/03/2026 22:00", "09/04/2026 05:30", "GREAT")));
    }

    @Test
    public void rejectsNonPositiveInterval() {
        assertThrows(InvalidRequestException.class, () -> validator.validateCreateRequest(
                new CreateSleepLogRequest("09/04/2026 05:30", "09/04/2026 05:30", "OK")));
    }

    @Test
    public void rejectsEndDateBeforeStartDate() {
        assertThrows(InvalidRequestException.class, () -> validator.validateCreateRequest(
                new CreateSleepLogRequest("09/04/2026 05:30", "09/03/2026 22:00", "OK")));
    }
}
