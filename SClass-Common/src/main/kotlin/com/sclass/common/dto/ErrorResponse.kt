package com.sclass.common.dto

import com.sclass.common.exception.ErrorCode

data class ErrorResponse(
    val code: String,
    val message: String,
    val status: Int,
) {
    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse =
            ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
                status = errorCode.httpStatus,
            )
    }
}
