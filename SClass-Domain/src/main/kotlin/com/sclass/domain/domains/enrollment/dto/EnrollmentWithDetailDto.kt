package com.sclass.domain.domains.enrollment.dto

import com.sclass.domain.domains.enrollment.domain.Enrollment

data class EnrollmentWithDetailDto(
    val enrollment: Enrollment,
    val studentName: String,
    val courseName: String,
    val teacherName: String,
)
