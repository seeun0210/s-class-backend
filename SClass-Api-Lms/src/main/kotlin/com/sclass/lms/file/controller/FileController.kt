package com.sclass.lms.file.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.lms.file.dto.DownloadUrlResponse
import com.sclass.lms.file.usecase.DeleteFileUseCase
import com.sclass.lms.file.usecase.GetFileDownloadUrlUseCase
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val deleteFileUseCase: DeleteFileUseCase,
    private val getFileDownloadUrlUseCase: GetFileDownloadUrlUseCase,
) {
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
