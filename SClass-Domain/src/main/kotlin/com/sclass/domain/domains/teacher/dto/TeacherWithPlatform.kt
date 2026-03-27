package com.sclass.domain.domains.teacher.dto

import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRoleState

data class TeacherWithPlatform(
    val teacher: Teacher,
    val platform: Platform,
    val state: UserRoleState,
)
