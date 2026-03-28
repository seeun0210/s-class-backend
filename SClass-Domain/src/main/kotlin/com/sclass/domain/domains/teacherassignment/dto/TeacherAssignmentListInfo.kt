package com.sclass.domain.domains.teacherassignment.dto

import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class TeacherAssignmentListInfo(
    val assignmentId: Long,
    val studentUserId: String,
    val studentName: String,
    val teacherUserId: String,
    val teacherName: String,
    val platform: Platform,
    val organizationId: Long?,
    val organizationName: String?,
    val assignedAt: LocalDateTime,
)
