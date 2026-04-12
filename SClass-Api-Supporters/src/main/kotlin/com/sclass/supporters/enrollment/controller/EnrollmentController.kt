package com.sclass.supporters.enrollment.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.common.dto.ApiResponse.Companion.success
import com.sclass.supporters.enrollment.dto.EnrollmentWithStudentResponse
import com.sclass.supporters.enrollment.dto.MyEnrollmentResponse
import com.sclass.supporters.enrollment.dto.PrepareEnrollmentRequest
import com.sclass.supporters.enrollment.dto.PrepareEnrollmentResponse
import com.sclass.supporters.enrollment.usecase.GetCourseEnrollmentsUseCase
import com.sclass.supporters.enrollment.usecase.GetEnrollmentLessonsUseCase
import com.sclass.supporters.enrollment.usecase.GetMyEnrollmentsUseCase
import com.sclass.supporters.enrollment.usecase.PrepareEnrollmentUseCase
import com.sclass.supporters.lesson.dto.LessonResponse
import jakarta.validation.Valid
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
    private val prepareEnrollmentUseCase: PrepareEnrollmentUseCase,
    private val getMyEnrollmentsUseCase: GetMyEnrollmentsUseCase,
    private val getCourseEnrollmentsUseCase: GetCourseEnrollmentsUseCase,
    private val getEnrollmentLessonsUseCase: GetEnrollmentLessonsUseCase,
) {
    @PostMapping
    fun prepareEnrollment(
        @CurrentUserId userId: String,
        @RequestBody @Valid request: PrepareEnrollmentRequest,
    ): ApiResponse<PrepareEnrollmentResponse> = success(prepareEnrollmentUseCase.execute(userId, request.courseId, request.pgType))

    @GetMapping("/me")
    fun getMyEnrollments(
        @CurrentUserId userId: String,
    ): ApiResponse<List<MyEnrollmentResponse>> = success(getMyEnrollmentsUseCase.execute(userId))

    @GetMapping
    fun getCourseEnrollments(
        @CurrentUserId userId: String,
        @RequestParam courseId: Long,
    ): ApiResponse<List<EnrollmentWithStudentResponse>> = success(getCourseEnrollmentsUseCase.execute(userId, courseId))

    @GetMapping("/{enrollmentId}/lessons")
    fun getEnrollmentLessons(
        @CurrentUserId userId: String,
        @PathVariable enrollmentId: Long,
    ): ApiResponse<List<LessonResponse>> = success(getEnrollmentLessonsUseCase.execute(userId, enrollmentId))
}
