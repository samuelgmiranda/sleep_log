package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.service.SleepLogService
import com.noom.interview.fullstack.sleep.validation.SleepHistoryValidator
import com.noom.interview.fullstack.sleep.validation.SleepLogValidator
import com.noom.interview.fullstack.sleep.model.SleepLogDTO
import com.noom.interview.fullstack.sleep.model.SleepHistoryDTO
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/sleep-logs")
class SleepLogController(
    private val sleepLogService: SleepLogService,
    private val sleepLogValidator: SleepLogValidator,
    private val sleepHistoryValidator: SleepHistoryValidator
) : BaseController() {

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun createSleepLog(
        httpRequest: HttpServletRequest,
        @RequestBody request: CreateSleepLogRequest
    ): CreateSleepLogResponse {
        sleepLogValidator.validateCreateRequest(request)
        sleepLogService.createSleepLog(getUserId(httpRequest), request)
        return CreateSleepLogResponse("saved successfully")
    }

    @GetMapping
    fun getSleepLog(
        httpRequest: HttpServletRequest,
        @RequestParam(required = false) targetDate: String?
    ): SleepLogDTO {
        val date = sleepLogValidator.validateTargetDate(targetDate)
        return sleepLogService.getSleepLog(getUserId(httpRequest), date)
    }

    @GetMapping("/history")
    fun getSleepHistory(
        httpRequest: HttpServletRequest,
        @RequestParam(name = "historyDays", required = false) historyDays: String?
    ): SleepHistoryDTO {
        val effectiveHistoryDays = sleepHistoryValidator.validateHistoryDays(historyDays)
        return sleepLogService.getSleepHistory(getUserId(httpRequest), effectiveHistoryDays)
    }
}

data class CreateSleepLogRequest(
    val startDate: String?,
    val endDate: String?,
    val userFeel: String?
)

data class CreateSleepLogResponse(val message: String)
