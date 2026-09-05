package com.noom.interview.fullstack.sleep.controller;

import com.noom.interview.fullstack.sleep.model.SleepHistoryDTO;
import com.noom.interview.fullstack.sleep.service.SleepLogService;
import com.noom.interview.fullstack.sleep.validation.BaseControllerValidator;
import com.noom.interview.fullstack.sleep.validation.SleepHistoryValidator;
import com.noom.interview.fullstack.sleep.validation.SleepLogValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SleepLogController.class)
@Import({BaseControllerValidator.class, SleepHistoryValidator.class, SleepLogValidator.class})
class SleepLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SleepLogService sleepLogService;

    @BeforeEach
    void setUp() {
        when(sleepLogService.getSleepHistory(anyInt(), anyInt())).thenReturn(historyResponse());
    }

    @Test
    void forwardsExplicitHistoryDaysToTheService() throws Exception {
        mockMvc.perform(get("/sleep-logs/history")
                        .header("X-User-Id", "2")
                        .queryParam("historyDays", "4000"))
                .andExpect(status().isOk());

        verify(sleepLogService).getSleepHistory(2, 4000);
    }

    @Test
    void defaultsHistoryDaysWhenTheQueryParameterIsOmitted() throws Exception {
        mockMvc.perform(get("/sleep-logs/history")
                        .header("X-User-Id", "2"))
                .andExpect(status().isOk());

        verify(sleepLogService).getSleepHistory(2, 30);
    }

    private SleepHistoryDTO historyResponse() {
        return new SleepHistoryDTO(
                2,
                "Sep 1st",
                "Sep 5th",
                "08:00",
                "10:00 pm",
                "06:00 am",
                java.util.Map.of("BAD", 0, "OK", 0, "GOOD", 0)
        );
    }
}
