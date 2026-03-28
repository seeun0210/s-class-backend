package com.sclass.domain.domains.teacherassignment.dto

import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class AssignedTeacherInfo(
    val assignmentId: Long,
    val teacherUserId: String,
    val teacherName: String,
    val platform: Platform,
    val organizationId: Long?,
    val organizationName: String?,
    val assignedAt: LocalDateTime,
)
