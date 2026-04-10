package com.sclass.backoffice.course.controller

import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.backoffice.course.dto.CreateBulkCourseRequest
import com.sclass.backoffice.course.dto.CreateCourseRequest
import com.sclass.backoffice.course.usecase.CreateBulkCourseUseCase
import com.sclass.backoffice.course.usecase.CreateCourseUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
class CourseController(
    private val createCourseUseCase: CreateCourseUseCase,
    private val createBulkCourseUseCase: CreateBulkCourseUseCase,
) {
    @PostMapping
    fun createCourse(
        @RequestBody @Valid request: CreateCourseRequest,
    ): ApiResponse<CourseResponse> = ApiResponse.success(createCourseUseCase.execute(request))

    @PostMapping("/bulk")
    fun createBulkCourse(
        @RequestBody @Valid request: CreateBulkCourseRequest,
    ): ApiResponse<List<CourseResponse>> = ApiResponse.success(createBulkCourseUseCase.execute(request))
}
