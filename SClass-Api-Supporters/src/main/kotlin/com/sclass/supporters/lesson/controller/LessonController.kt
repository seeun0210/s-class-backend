package com.sclass.supporters.lesson.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import com.sclass.supporters.lesson.dto.CreateLessonInquiryPlanRequest
import com.sclass.supporters.lesson.usecase.CreateLessonInquiryPlanUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lessons")
class LessonController(
    private val createLessonInquiryPlanUseCase: CreateLessonInquiryPlanUseCase,
) {
    @PostMapping("/{lessonId}/inquiry-plans")
    fun createInquiryPlan(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: CreateLessonInquiryPlanRequest,
    ): ApiResponse<InquiryPlanResponse> = ApiResponse.success(createLessonInquiryPlanUseCase.execute(userId, lessonId, request))
}
