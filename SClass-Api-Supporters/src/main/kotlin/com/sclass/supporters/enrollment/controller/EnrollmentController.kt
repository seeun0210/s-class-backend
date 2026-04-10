package com.sclass.supporters.enrollment.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.enrollment.dto.PrepareEnrollmentRequest
import com.sclass.supporters.enrollment.dto.PrepareEnrollmentResponse
import com.sclass.supporters.enrollment.usecase.PrepareEnrollmentUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/enrollments")
class EnrollmentController(
    private val prepareEnrollmentUseCase: PrepareEnrollmentUseCase,
) {
    @PostMapping
    fun prepareEnrollment(
        @CurrentUserId userId: String,
        @RequestBody @Valid request: PrepareEnrollmentRequest,
    ): ApiResponse<PrepareEnrollmentResponse> =
        ApiResponse.success(prepareEnrollmentUseCase.execute(userId, request.courseId, request.pgType))
}
