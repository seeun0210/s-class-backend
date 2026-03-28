package com.sclass.domain.domains.student.dto

import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.user.domain.UserRole

data class StudentWithRoles(
    val student: Student,
    val roles: List<UserRole>,
)
