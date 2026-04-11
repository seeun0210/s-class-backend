package com.sclass.supporters.course.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.course.dto.CourseResponse
import com.sclass.supporters.course.usecase.GetCourseListUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
class CourseController(
    private val getCourseListUseCase: GetCourseListUseCase,
) {
    @GetMapping
    fun getCourseList(): ApiResponse<List<CourseResponse>> = ApiResponse.success(getCourseListUseCase.execute())
}
