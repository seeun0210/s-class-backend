package com.sclass.lms.file.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.lms.file.dto.FileResponse
import com.sclass.lms.file.usecase.ReadFileUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val readFileUseCase: ReadFileUseCase,
) {
    @GetMapping("/{fileId}")
    fun getFile(
        @PathVariable fileId: String,
    ): ApiResponse<FileResponse> {
        val file = readFileUseCase.getFile(fileId)
        return ApiResponse.success(FileResponse.from(file))
    }
}
