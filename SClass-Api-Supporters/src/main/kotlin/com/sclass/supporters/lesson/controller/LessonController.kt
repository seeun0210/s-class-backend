package com.sclass.supporters.lesson.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import com.sclass.supporters.lesson.dto.CreateLessonInquiryPlanRequest
import com.sclass.supporters.lesson.dto.LessonDetailResponse
import com.sclass.supporters.lesson.dto.LessonResponse
import com.sclass.supporters.lesson.dto.UpdateLessonRequest
import com.sclass.supporters.lesson.usecase.CreateLessonInquiryPlanUseCase
import com.sclass.supporters.lesson.usecase.GetLessonDetailUseCase
import com.sclass.supporters.lesson.usecase.UpdateLessonUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lessons")
class LessonController(
    private val createLessonInquiryPlanUseCase: CreateLessonInquiryPlanUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val updateLessonUseCase: UpdateLessonUseCase,
) {
    @GetMapping("/{lessonId}")
    fun detail(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
    ): ApiResponse<LessonDetailResponse> = ApiResponse.success(getLessonDetailUseCase.execute(userId, lessonId))

    @PatchMapping("/{lessonId}")
    fun update(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: UpdateLessonRequest,
    ): ApiResponse<LessonResponse> = ApiResponse.success(updateLessonUseCase.execute(userId, lessonId, request))

    @PostMapping("/{lessonId}/inquiry-plans")
    fun createInquiryPlan(
        @CurrentUserId userId: String,
        @PathVariable lessonId: Long,
        @Valid @RequestBody request: CreateLessonInquiryPlanRequest,
    ): ApiResponse<InquiryPlanResponse> = ApiResponse.success(createLessonInquiryPlanUseCase.execute(userId, lessonId, request))
}
