package com.sclass.domain.domains.student.dto

import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRoleState

data class StudentWithPlatform(
    val student: Student,
    val platform: Platform,
    val state: UserRoleState,
)
