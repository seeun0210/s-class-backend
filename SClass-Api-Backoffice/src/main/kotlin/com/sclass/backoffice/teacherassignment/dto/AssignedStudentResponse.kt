package com.sclass.backoffice.teacherassignment.dto

import com.sclass.domain.domains.teacherassignment.domain.TeacherAssignment
import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class AssignedStudentResponse(
    val id: Long,
    val studentId: String,
    val studentName: String,
    val platform: Platform,
    val organizationId: Long?,
    val organizationName: String?,
    val assignedAt: LocalDateTime,
) {
    companion object {
        fun from(
            assignment: TeacherAssignment,
            studentName: String,
            organizationName: String?,
        ): AssignedStudentResponse =
            AssignedStudentResponse(
                id = assignment.id,
                studentId = assignment.studentId,
                studentName = studentName,
                platform = assignment.platform,
                organizationId = assignment.organizationId,
                organizationName = organizationName,
                assignedAt = assignment.assignedAt,
            )
    }
}
