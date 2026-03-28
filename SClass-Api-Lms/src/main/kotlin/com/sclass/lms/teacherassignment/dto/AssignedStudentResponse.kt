package com.sclass.lms.teacherassignment.dto

import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.teacherassignment.dto.AssignedStudentInfo
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import java.time.LocalDateTime

data class AssignedStudentResponse(
    val assignmentId: Long,
    val studentUserId: String,
    val studentName: String,
    val grade: Grade?,
    val school: String?,
    val platform: Platform,
    val organizationId: Long?,
    val organizationName: String?,
    val assignedAt: LocalDateTime,
    val documents: List<AssignedStudentDocumentResponse>,
) {
    companion object {
        fun from(
            info: AssignedStudentInfo,
            documents: List<StudentDocument>,
        ): AssignedStudentResponse =
            AssignedStudentResponse(
                assignmentId = info.assignmentId,
                studentUserId = info.studentUserId,
                studentName = info.studentName,
                grade = info.grade,
                school = info.school,
                platform = info.platform,
                organizationId = info.organizationId,
                organizationName = info.organizationName,
                assignedAt = info.assignedAt,
                documents = documents.map { AssignedStudentDocumentResponse.from(it) },
            )
    }
}
