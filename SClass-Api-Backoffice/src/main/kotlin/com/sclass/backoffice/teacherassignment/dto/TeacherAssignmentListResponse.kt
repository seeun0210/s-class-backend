package com.sclass.backoffice.teacherassignment.dto

import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentListInfo
import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class TeacherAssignmentListResponse(
    val id: Long,
    val studentUserId: String,
    val studentName: String,
    val teacherUserId: String,
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
                studentUserId = info.studentUserId,
                studentName = info.studentName,
                teacherUserId = info.teacherUserId,
                teacherName = info.teacherName,
                platform = info.platform,
                organizationId = info.organizationId,
                organizationName = info.organizationName,
                assignedAt = info.assignedAt,
            )
    }
}
