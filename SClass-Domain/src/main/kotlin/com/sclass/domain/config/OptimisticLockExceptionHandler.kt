package com.sclass.domain.config

import com.sclass.common.dto.ApiResponse
import com.sclass.common.dto.ErrorResponse
import com.sclass.common.exception.GlobalErrorCode
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class OptimisticLockExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun handleOptimisticLock(e: OptimisticLockingFailureException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Optimistic lock conflict: {}", e.message)
        val errorCode = GlobalErrorCode.CONFLICT
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(ApiResponse.error(ErrorResponse.of(errorCode)))
    }
}
