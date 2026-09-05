package com.noom.interview.fullstack.sleep.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(InvalidRequestException::class, BusinessValidationException::class)
    fun handleBadRequest(exception: RuntimeException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError(message = exception.message ?: "Invalid request"))

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(exception: ResourceNotFoundException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError(message = exception.message ?: "Resource not found"))

    @ExceptionHandler(Exception::class)
    fun handleInternalError(): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError(message = "Internal server error"))
}

data class ApiError(
    val status: String = "error",
    val message: String
)