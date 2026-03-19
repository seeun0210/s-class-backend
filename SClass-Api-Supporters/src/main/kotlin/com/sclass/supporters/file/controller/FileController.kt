package com.sclass.supporters.file.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.file.dto.PresignedUrlRequest
import com.sclass.supporters.file.dto.PresignedUrlResponse
import com.sclass.supporters.file.usecase.CreateFileUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val createFileUseCase: CreateFileUseCase,
) {
    @PostMapping("/presigned-url")
    fun createPresignedUrl(
        @RequestHeader("X-User-Id") uploadedBy: String,
        @Valid @RequestBody request: PresignedUrlRequest,
    ): ApiResponse<PresignedUrlResponse> {
        val response =
            createFileUseCase.execute(
                uploadedBy = uploadedBy,
                originalFilename = request.originalFilename,
                contentType = request.contentType,
                fileSize = request.fileSize,
                fileType = request.fileType,
            )
        return ApiResponse.success(response)
    }
}
