

import javax.servlet.http.HttpServletRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

abstract class BaseController {

    protected fun getUserId(request: HttpServletRequest): Int {
        val userIdHeader = request.getHeader("X-User-Id")
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "X-User-Id header is required"
            )

        try {
            return userIdHeader.toInt()
        } catch (e: NumberFormatException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "X-User-Id header must be a valid integer"
            )
        }
    }
}