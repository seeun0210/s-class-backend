package com.sclass.backoffice.lessonReport.controller

import com.sclass.backoffice.lessonReport.dto.LessonReportResponse
import com.sclass.backoffice.lessonReport.dto.ReviewLessonReportRequest
import com.sclass.backoffice.lessonReport.usecase.ReviewLessonReportUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lesson-reports")
class LessonReportController(
    private val reviewLessonReportUseCase: ReviewLessonReportUseCase,
) {
    @PatchMapping("/{reportId}/review")
    fun review(
        @CurrentUserId userId: String,
        @PathVariable reportId: Long,
        @Valid @RequestBody request: ReviewLessonReportRequest,
    ): ApiResponse<LessonReportResponse> = ApiResponse.success(reviewLessonReportUseCase.execute(userId, reportId, request))
}
