package com.sclass.domain.domains.enrollment.dto

import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.user.domain.User

data class EnrollmentWithStudentDto(
    val enrollment: Enrollment,
    val student: User?,
)
