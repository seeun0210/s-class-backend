package com.sclass.common.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> = ApiResponse(success = true, data = data)

        fun error(error: ErrorResponse): ApiResponse<Nothing> = ApiResponse(success = false, error = error)
    }
}
