package com.sclass.common.dto

import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> = ApiResponse(success = true, data = data)

        fun <T> success(
            data: T? = null,
            cacheControl: CacheControl,
        ): ResponseEntity<ApiResponse<T>> =
            ResponseEntity
                .ok()
                .cacheControl(cacheControl)
                .body(success(data))

        fun error(error: ErrorResponse): ApiResponse<Nothing> = ApiResponse(success = false, error = error)
    }
}
