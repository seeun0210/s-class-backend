package com.sclass.domain.domains.course.dto

import com.sclass.domain.domains.course.domain.Course

data class CourseWithTeacherAndEnrollmentCountDto(
    val course: Course,
    val teacherName: String,
    val enrollmentCount: Long,
)
