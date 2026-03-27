package com.sclass.domain.domains.student.dto

import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.UserRoleState
import java.time.LocalDateTime

data class StudentSearchCondition(
    val name: String? = null,
    val email: String? = null,
    val grade: Grade? = null,
    val school: String? = null,
    val state: UserRoleState? = null,
    val platform: Platform? = null,
    val createdAtFrom: LocalDateTime? = null,
    val createdAtTo: LocalDateTime? = null,
)
