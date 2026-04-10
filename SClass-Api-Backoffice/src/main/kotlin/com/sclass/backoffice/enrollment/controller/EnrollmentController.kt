package com.sclass.backoffice.enrollment.controller

import com.sclass.backoffice.enrollment.dto.EnrollmentResponse
import com.sclass.backoffice.enrollment.dto.GrantEnrollmentRequest
import com.sclass.backoffice.enrollment.usecase.GrantEnrollmentUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/enrollments")
class EnrollmentController(
    private val grantEnrollmentUseCase: GrantEnrollmentUseCase,
) {
    @PostMapping("/grant")
    fun grantEnrollment(
        @CurrentUserId userId: String,
        @RequestBody @Valid request: GrantEnrollmentRequest,
    ): ApiResponse<EnrollmentResponse> =
        ApiResponse.success(grantEnrollmentUseCase.execute(userId, request.studentUserId, request.courseId, request.grantReason))
}
