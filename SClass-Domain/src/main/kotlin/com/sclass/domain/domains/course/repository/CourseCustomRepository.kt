package com.sclass.domain.domains.course.repository

import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CatalogCourseDto
import com.sclass.domain.domains.course.dto.CourseWithEnrollmentCountDto
import com.sclass.domain.domains.course.dto.CourseWithTeacherAndEnrollmentCountDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CourseCustomRepository {
    fun findCourseDetailById(id: Long): CourseWithTeacherAndEnrollmentCountDto?

    fun findAllByTeacherUserIdWithEnrollmentCount(teacherUserId: String): List<CourseWithEnrollmentCountDto>

    fun searchCourses(
        teacherId: String?,
        status: CourseStatus?,
        pageable: Pageable,
    ): Page<CourseWithTeacherAndEnrollmentCountDto>

    fun findAllCatalogCoursesByProductIds(productIds: Collection<String>): List<CatalogCourseDto>
}
