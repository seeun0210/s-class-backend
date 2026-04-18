package com.sclass.domain.domains.course.dto

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.product.domain.CourseProduct

data class CourseWithEnrollmentCountDto(
    val course: Course,
    val courseProduct: CourseProduct?,
    val enrollmentCount: Long,
)
