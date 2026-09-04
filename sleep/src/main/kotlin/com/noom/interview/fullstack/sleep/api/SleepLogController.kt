package com.noom.interview.fullstack.sleep.api

import com.noom.interview.fullstack.sleep.BaseController
import com.noom.interview.fullstack.sleep.model.UserFeel
import com.noom.interview.fullstack.sleep.service.SleepLogService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/sleep-logs")
class SleepLogController(
    private val sleepLogService: SleepLogService
) : BaseController() {

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun createSleepLog(
        httpRequest: HttpServletRequest,
        @RequestBody request: CreateSleepLogRequest
    ): CreateSleepLogResponse {
        sleepLogService.createSleepLog(getUserId(httpRequest), request)
        return CreateSleepLogResponse("saved successfully")
    }
}

data class CreateSleepLogRequest(
    val startDate: String?,
    val endDate: String?,
    val userFeel: UserFeel?
)

data class CreateSleepLogResponse(val message: String)
