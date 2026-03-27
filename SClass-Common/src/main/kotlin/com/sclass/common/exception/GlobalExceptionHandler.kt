package com.sclass.common.exception

import com.sclass.common.dto.ApiResponse
import com.sclass.common.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
open class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = e.errorCode
        log.warn("BusinessException: [{}] {}", errorCode.code, errorCode.message)
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(ApiResponse.error(ErrorResponse.of(errorCode)))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val message =
            e.bindingResult.fieldErrors.joinToString(", ") {
                "${it.field}: ${it.defaultMessage}"
            }
        log.warn("Validation failed: {}", message)
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(ErrorResponse(GlobalErrorCode.INVALID_INPUT.code, message, 400)))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        val businessException =
            generateSequence(e as Throwable) { it.cause }
                .filterIsInstance<BusinessException>()
                .firstOrNull()
        if (businessException != null) {
            return handleBusinessException(businessException)
        }
        log.warn("Message not readable: {}", e.message)
        val errorCode = GlobalErrorCode.INVALID_INPUT
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(ErrorResponse.of(errorCode)))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unhandled exception", e)
        val errorCode = GlobalErrorCode.INTERNAL_ERROR
        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.error(ErrorResponse.of(errorCode)))
    }
}
