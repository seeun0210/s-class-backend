package com.sclass.backoffice.course.controller

import com.sclass.backoffice.course.dto.ChangeCourseStatusRequest
import com.sclass.backoffice.course.dto.CoursePageResponse
import com.sclass.backoffice.course.dto.CourseResponse
import com.sclass.backoffice.course.dto.CreateCourseRequest
import com.sclass.backoffice.course.usecase.ChangeCourseStatusUseCase
import com.sclass.backoffice.course.usecase.CreateCourseUseCase
import com.sclass.backoffice.course.usecase.GetCourseListUseCase
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.course.domain.CourseStatus
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
class CourseController(
    private val createCourseUseCase: CreateCourseUseCase,
    private val changeCourseStatusUseCase: ChangeCourseStatusUseCase,
    private val getCourseListUseCase: GetCourseListUseCase,
) {
    @PostMapping
    fun createCourse(
        @RequestBody @Valid request: CreateCourseRequest,
    ): ApiResponse<CourseResponse> = ApiResponse.success(createCourseUseCase.execute(request))

    @PatchMapping("/{courseId}/status")
    fun changeCourseStatus(
        @PathVariable courseId: Long,
        @RequestBody @Valid request: ChangeCourseStatusRequest,
    ): ApiResponse<CourseResponse> = ApiResponse.success(changeCourseStatusUseCase.execute(courseId, request.status))

    @GetMapping
    fun getCourseList(
        @RequestParam(required = false) teacherUserId: String?,
        @RequestParam(required = false) status: CourseStatus?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<CoursePageResponse> = ApiResponse.success(getCourseListUseCase.execute(teacherUserId, status, pageable))
}
