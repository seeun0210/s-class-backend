package com.sclass.domain.domains.enrollment.dto

import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.product.domain.CourseProduct

data class EnrollmentWithCourseDto(
    val enrollment: Enrollment,
    val course: Course?,
    val courseProduct: CourseProduct?,
    val teacherName: String?,
)
