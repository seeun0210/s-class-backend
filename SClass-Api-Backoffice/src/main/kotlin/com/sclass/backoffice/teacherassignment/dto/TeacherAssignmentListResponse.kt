package com.sclass.backoffice.teacherassignment.dto

import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentListInfo
import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class TeacherAssignmentListResponse(
    val id: Long,
    val studentId: String,
    val studentName: String,
    val teacherId: String,
    val teacherName: String,
    val platform: Platform,
    val organizationId: Long?,
    val organizationName: String?,
    val assignedAt: LocalDateTime,
) {
    companion object {
        fun from(info: TeacherAssignmentListInfo): TeacherAssignmentListResponse =
            TeacherAssignmentListResponse(
                id = info.assignmentId,
                studentId = info.studentUserId,
                studentName = info.studentName,
                teacherId = info.teacherUserId,
                teacherName = info.teacherName,
                platform = info.platform,
                organizationId = info.organizationId,
                organizationName = info.organizationName,
                assignedAt = info.assignedAt,
            )
    }
}
