package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.validation.BaseControllerValidator
import javax.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseController {

    @Autowired
    private lateinit var baseControllerValidator: BaseControllerValidator

    protected fun getUserId(request: HttpServletRequest): Int {
        return baseControllerValidator.validateUserId(request.getHeader("X-User-Id"))
    }
}