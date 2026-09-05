package com.noom.interview.fullstack.sleep.exception

class InvalidRequestException(message: String) : RuntimeException(message)

class BusinessValidationException(message: String) : RuntimeException(message)

class ResourceNotFoundException(message: String) : RuntimeException(message)