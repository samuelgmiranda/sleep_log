/*
 * Copyright (C) 2023 Noom, Inc.
 */
package com.noom.interview.fullstack.sleep

import javax.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
@RestController
class TestController : BaseController() {
    
    @GetMapping("/test")
    fun test(httpRequest: HttpServletRequest,) : Map<String, String> {
        return mapOf(
            "testMessage" to "Hi ${getUserId(httpRequest)}! Hello world!"
        )
    }
}
