package com.sclass.backoffice.lesson.controller

import com.sclass.backoffice.lesson.dto.LessonResponse
import com.sclass.backoffice.lesson.dto.UpdateSubstituteTeacherRequest
import com.sclass.backoffice.lesson.usecase.GetLessonDetailUseCase
import com.sclass.backoffice.lesson.usecase.UpdateSubstituteTeacherUseCase
import com.sclass.backoffice.lessonReport.dto.LessonReportResponse
import com.sclass.backoffice.lessonReport.usecase.GetLessonReportUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lessons")
class LessonController(
    private val updateSubstituteTeacherUseCase: UpdateSubstituteTeacherUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val getLessonReportUseCase: GetLessonReportUseCase,
) {
    @GetMapping("/{lessonId}")
    fun getDetail(
        @PathVariable lessonId: Long,
    ): ApiResponse<LessonResponse> = ApiResponse.success(getLessonDetailUseCase.execute(lessonId))

    @PatchMapping("/{lessonId}/substitute-teacher")
    fun updateSubstitute(
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: UpdateSubstituteTeacherRequest,
    ): ApiResponse<LessonResponse> = ApiResponse.success(updateSubstituteTeacherUseCase.execute(lessonId, request))

    @GetMapping("/{lessonId}/report")
    fun getReport(
        @PathVariable lessonId: Long,
    ): ApiResponse<LessonReportResponse> = ApiResponse.success(getLessonReportUseCase.execute(lessonId))
}
