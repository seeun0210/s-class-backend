package com.sclass.domain.domains.course.repository

import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CourseWithTeacherAndEnrollmentCountDto
import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CourseCustomRepository {
    fun findAllActiveWithTeacher(): List<CourseWithTeacherDto>

    fun searchCourses(
        teacherId: String?,
        status: CourseStatus?,
        pageable: Pageable,
    ): Page<CourseWithTeacherAndEnrollmentCountDto>
}
