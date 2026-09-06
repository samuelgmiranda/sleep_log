package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.model.SleepHistoryDTO
import com.noom.interview.fullstack.sleep.service.SleepLogService
import com.noom.interview.fullstack.sleep.validation.BaseControllerValidator
import com.noom.interview.fullstack.sleep.validation.SleepHistoryValidator
import com.noom.interview.fullstack.sleep.validation.SleepLogValidator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [SleepLogController::class])
@Import(BaseControllerValidator::class, SleepHistoryValidator::class, SleepLogValidator::class)
class SleepLogControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var sleepLogService: SleepLogService

    @BeforeEach
    fun setUp() {
        `when`(sleepLogService.getSleepHistory(anyInt(), anyInt())).thenReturn(historyResponse())
    }

    @Test
    fun forwardsExplicitHistoryDaysToTheService() {
        mockMvc.perform(get("/sleep-logs/history").header("X-User-Id", "2").queryParam("historyDays", "365"))
            .andExpect(status().isOk)

        verify(sleepLogService).getSleepHistory(2, 365)
    }

    @Test
    fun defaultsHistoryDaysWhenTheQueryParameterIsOmitted() {
        mockMvc.perform(get("/sleep-logs/history").header("X-User-Id", "2"))
            .andExpect(status().isOk)

        verify(sleepLogService).getSleepHistory(2, 30)
    }

    @Test
    fun rejectsInvalidHistoryDaysWithTheErrorContract() {
        assertInvalidHistoryDays("0", "historyDays must be greater than zero")
        assertInvalidHistoryDays("-1", "historyDays must be greater than zero")
        assertInvalidHistoryDays("30.5", "historyDays must be an integer")
        assertInvalidHistoryDays("", "historyDays must be an integer")
        assertInvalidHistoryDays("thirty", "historyDays must be an integer")
        assertInvalidHistoryDays("366", "historyDays must not exceed 365")
    }

    @Test
    fun serializesMidnightNoonAndLowercaseMeridians() {
        `when`(sleepLogService.getSleepHistory(2, 1)).thenReturn(
            SleepHistoryDTO(2, "Sep 5th", "Sep 5th", "08:00", "12:00 am", "12:00 pm", mapOf("BAD" to 0, "OK" to 1, "GOOD" to 0))
        )

        mockMvc.perform(get("/sleep-logs/history").header("X-User-Id", "2").queryParam("historyDays", "1"))
            .andExpect(status().isOk)
            .andExpect(content().json("{\"userId\":2,\"dateRangeStart\":\"Sep 5th\",\"dateRangeEnd\":\"Sep 5th\",\"averageDuration\":\"08:00\",\"averageStart\":\"12:00 am\",\"averageEnd\":\"12:00 pm\",\"userFeelTotals\":{\"BAD\":0,\"OK\":1,\"GOOD\":0}}", true))
    }

    private fun assertInvalidHistoryDays(historyDays: String, message: String) {
        mockMvc.perform(get("/sleep-logs/history").header("X-User-Id", "2").queryParam("historyDays", historyDays))
            .andExpect(status().isBadRequest)
            .andExpect(content().json("{\"status\":\"error\",\"message\":\"$message\"}", true))
    }

    private fun historyResponse() = SleepHistoryDTO(
        2, "Sep 1st", "Sep 5th", "08:00", "10:00 pm", "06:00 am", mapOf("BAD" to 0, "OK" to 0, "GOOD" to 0)
    )
}
