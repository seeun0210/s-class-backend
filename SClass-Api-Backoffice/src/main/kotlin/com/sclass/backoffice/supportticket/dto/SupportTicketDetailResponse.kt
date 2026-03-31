package com.sclass.backoffice.supportticket.dto

import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.commission.domain.SupportTicketType
import com.sclass.domain.domains.commission.domain.TicketStatus
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.User
import java.time.LocalDateTime

data class SupportTicketDetailResponse(
    val id: Long,
    val commissionId: Long,
    val type: SupportTicketType,
    val reason: String,
    val status: TicketStatus,
    val teacher: TeacherSummary,
    val student: StudentDetail,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(
            ticket: CommissionSupportTicket,
            teacherUser: User,
            student: Student,
            transcript: StudentDocument?,
        ) = SupportTicketDetailResponse(
            id = ticket.id,
            commissionId = ticket.commission.id,
            type = ticket.type,
            reason = ticket.reason,
            status = ticket.status,
            teacher = TeacherSummary.from(teacherUser),
            student = StudentDetail.from(student, transcript),
            createdAt = ticket.createdAt,
        )
    }
}

data class TeacherSummary(
    val userId: String,
    val name: String,
    val email: String,
) {
    companion object {
        fun from(user: User) =
            TeacherSummary(
                userId = user.id,
                name = user.name,
                email = user.email,
            )
    }
}

data class StudentDetail(
    val userId: String,
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val grade: Grade?,
    val school: String?,
    val parentPhoneNumber: String?,
    val transcript: TranscriptInfo?,
) {
    companion object {
        fun from(
            student: Student,
            transcript: StudentDocument?,
        ) = StudentDetail(
            userId = student.user.id,
            name = student.user.name,
            email = student.user.email,
            phoneNumber = student.user.phoneNumber,
            grade = student.grade,
            school = student.school,
            parentPhoneNumber = student.parentPhoneNumber,
            transcript = transcript?.let { TranscriptInfo.from(it) },
        )
    }
}

data class TranscriptInfo(
    val id: String,
    val originalFilename: String,
    val mimeType: String,
    val fileSize: Long,
) {
    companion object {
        fun from(document: StudentDocument) =
            TranscriptInfo(
                id = document.id,
                originalFilename = document.file.originalFilename,
                mimeType = document.file.mimeType,
                fileSize = document.file.fileSize,
            )
    }
}
