package com.sclass.domain.domains.course.dto

import com.sclass.domain.domains.course.domain.Course

data class CourseWithEnrollmentCountDto(
    val course: Course,
    val enrollmentCount: Long,
)
