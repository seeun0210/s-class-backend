package com.sclass.domain.domains.teacher.dto

import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.user.domain.UserRole

data class TeacherWithRoles(
    val teacher: Teacher,
    val roles: List<UserRole>,
)
