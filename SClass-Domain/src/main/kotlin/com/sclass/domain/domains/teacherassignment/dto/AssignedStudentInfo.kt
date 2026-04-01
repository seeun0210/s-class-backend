package com.sclass.domain.domains.teacherassignment.dto

import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class AssignedStudentInfo(
    val assignmentId: Long,
    val studentUserId: String,
    val studentName: String,
    val grade: Grade?,
    val school: String?,
    val platform: Platform,
    val organizationId: Long?,
    val organizationName: String?,
    val assignedAt: LocalDateTime,
)
