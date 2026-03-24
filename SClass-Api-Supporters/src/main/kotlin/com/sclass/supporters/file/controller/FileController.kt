package com.sclass.supporters.file.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.file.dto.DownloadUrlResponse
import com.sclass.supporters.file.dto.PresignedUrlRequest
import com.sclass.supporters.file.dto.PresignedUrlResponse
import com.sclass.supporters.file.usecase.CreateFileUseCase
import com.sclass.supporters.file.usecase.DeleteFileUseCase
import com.sclass.supporters.file.usecase.GetFileDownloadUrlUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val createFileUseCase: CreateFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val getFileDownloadUrlUseCase: GetFileDownloadUrlUseCase,
) {
    @PostMapping("/presigned-url")
    fun createPresignedUrl(
        @CurrentUserId userId: String,
        @Valid @RequestBody request: PresignedUrlRequest,
    ): ApiResponse<PresignedUrlResponse> {
        val response =
            createFileUseCase.execute(
                uploadedBy = userId,
                originalFilename = request.originalFilename,
                contentType = request.contentType,
                fileSize = request.fileSize,
                fileType = request.fileType,
            )
        return ApiResponse.success(response)
    }

    @GetMapping("/{fileId}/download-url")
    fun getDownloadUrl(
        @PathVariable fileId: String,
    ): ApiResponse<DownloadUrlResponse> = ApiResponse.success(getFileDownloadUrlUseCase.execute(fileId))

    @DeleteMapping("/{fileId}")
    fun deleteFile(
        @PathVariable fileId: String,
    ): ApiResponse<Unit> {
        deleteFileUseCase.execute(fileId)
        return ApiResponse.success(Unit)
    }
}
