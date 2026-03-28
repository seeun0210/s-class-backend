package com.sclass.backoffice.teacherassignment.dto

import com.sclass.domain.domains.teacherassignment.domain.TeacherAssignment
import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class TeacherAssignmentResponse(
    val id: Long,
    val teacherId: String,
    val teacherName: String,
    val platform: Platform,
    val organizationId: Long?,
    val organizationName: String?,
    val assignedAt: LocalDateTime,
) {
    companion object {
        fun from(
            assignment: TeacherAssignment,
            teacherName: String,
            organizationName: String?,
        ): TeacherAssignmentResponse =
            TeacherAssignmentResponse(
                id = assignment.id,
                teacherId = assignment.teacherId,
                teacherName = teacherName,
                platform = assignment.platform,
                organizationId = assignment.organizationId,
                organizationName = organizationName,
                assignedAt = assignment.assignedAt,
            )
    }
}
