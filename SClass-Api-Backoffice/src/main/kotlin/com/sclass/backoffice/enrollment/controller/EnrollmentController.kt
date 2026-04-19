package com.sclass.backoffice.enrollment.controller

import com.sclass.backoffice.enrollment.dto.EnrollmentLessonListResponse
import com.sclass.backoffice.enrollment.dto.EnrollmentPageResponse
import com.sclass.backoffice.enrollment.dto.EnrollmentResponse
import com.sclass.backoffice.enrollment.dto.GrantEnrollmentRequest
import com.sclass.backoffice.enrollment.dto.GrantMembershipEnrollmentRequest
import com.sclass.backoffice.enrollment.usecase.GetEnrollmentLessonListUseCase
import com.sclass.backoffice.enrollment.usecase.GetEnrollmentListUseCase
import com.sclass.backoffice.enrollment.usecase.GrantEnrollmentUseCase
import com.sclass.backoffice.enrollment.usecase.GrantMembershipEnrollmentUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/enrollments")
class EnrollmentController(
    private val grantEnrollmentUseCase: GrantEnrollmentUseCase,
    private val grantMembershipEnrollmentUseCase: GrantMembershipEnrollmentUseCase,
    private val getEnrollmentListUseCase: GetEnrollmentListUseCase,
    private val getEnrollmentLessonListUseCase: GetEnrollmentLessonListUseCase,
) {
    @PostMapping("/membership")
    fun grantMembershipEnrollment(
        @CurrentUserId userId: String,
        @RequestBody @Valid request: GrantMembershipEnrollmentRequest,
    ): ApiResponse<EnrollmentResponse> = ApiResponse.success(grantMembershipEnrollmentUseCase.execute(userId, request))

    @PostMapping("/course")
    fun grantCourseEnrollment(
        @CurrentUserId userId: String,
        @RequestBody @Valid request: GrantEnrollmentRequest,
    ): ApiResponse<EnrollmentResponse> =
        ApiResponse.success(grantEnrollmentUseCase.execute(userId, request.studentUserId, request.courseId, request.grantReason))

    @GetMapping
    fun getEnrollmentList(
        @RequestParam(required = false) studentUserId: String?,
        @RequestParam(required = false) teacherUserId: String?,
        @RequestParam(required = false) courseId: Long?,
        @RequestParam(required = false) status: EnrollmentStatus?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<EnrollmentPageResponse> =
        ApiResponse.success(getEnrollmentListUseCase.execute(studentUserId, teacherUserId, courseId, status, pageable))

    @GetMapping("/{enrollmentId}/lessons")
    fun getEnrollmentLessonList(
        @PathVariable enrollmentId: Long,
    ): ApiResponse<EnrollmentLessonListResponse> = ApiResponse.success(getEnrollmentLessonListUseCase.execute(enrollmentId))
}
