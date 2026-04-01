package com.sclass.backoffice.supportticket.dto

import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.CommissionSupportTicket
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.commission.domain.SupportTicketType
import com.sclass.domain.domains.commission.domain.TicketStatus
import com.sclass.domain.domains.file.domain.FileType
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
    val response: String?,
    val teacher: TeacherSummary,
    val student: StudentDetail,
    val commission: CommissionInfo,
    val commissionFiles: List<CommissionFileInfo>,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(
            ticket: CommissionSupportTicket,
            teacherUser: User,
            student: Student,
            transcript: StudentDocument?,
            commissionFiles: List<CommissionFileInfo>,
        ) = SupportTicketDetailResponse(
            id = ticket.id,
            commissionId = ticket.commission.id,
            type = ticket.type,
            reason = ticket.reason,
            status = ticket.status,
            response = ticket.response,
            teacher = TeacherSummary.from(teacherUser),
            student = StudentDetail.from(student, transcript),
            commission = CommissionInfo.from(ticket),
            commissionFiles = commissionFiles,
            createdAt = ticket.createdAt,
        )
    }
}

data class CommissionInfo(
    val id: Long,
    val studentUserId: String,
    val teacherUserId: String,
    val outputFormat: OutputFormat,
    val activityType: ActivityType,
    val status: CommissionStatus,
    val guideInfo: GuideInfoInfo,
) {
    companion object {
        fun from(ticket: CommissionSupportTicket): CommissionInfo =
            with(ticket.commission) {
                CommissionInfo(
                    id = id,
                    studentUserId = studentUserId,
                    teacherUserId = teacherUserId,
                    outputFormat = outputFormat,
                    activityType = activityType,
                    status = status,
                    guideInfo =
                        with(guideInfo) {
                            GuideInfoInfo(
                                subject = subject,
                                volume = volume,
                                requiredElements = requiredElements,
                                gradingCriteria = gradingCriteria,
                                teacherEmphasis = teacherEmphasis,
                            )
                        },
                )
            }
    }
}

data class GuideInfoInfo(
    val subject: String,
    val volume: String,
    val requiredElements: String?,
    val gradingCriteria: String,
    val teacherEmphasis: String,
)

data class CommissionFileInfo(
    val id: Long,
    val fileMeta: FileMetaInfo,
) {
    companion object {
        fun from(commissionFile: CommissionFile): CommissionFileInfo =
            CommissionFileInfo(
                id = commissionFile.id,
                fileMeta =
                    FileMetaInfo(
                        id = commissionFile.file.id,
                        originalFilename = commissionFile.file.originalFilename,
                        mimeType = commissionFile.file.mimeType,
                        fileSize = commissionFile.file.fileSize,
                        fileType = commissionFile.file.fileType,
                        uploadedBy = commissionFile.file.uploadedBy,
                        createdAt = commissionFile.file.createdAt,
                    ),
            )
    }
}

data class FileMetaInfo(
    val id: String,
    val originalFilename: String,
    val mimeType: String,
    val fileSize: Long,
    val fileType: FileType,
    val uploadedBy: String,
    val createdAt: LocalDateTime,
)

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
    val documentId: String,
    val fileId: String,
    val originalFilename: String,
    val mimeType: String,
    val fileSize: Long,
) {
    companion object {
        fun from(document: StudentDocument) =
            TranscriptInfo(
                id = document.id,
                documentId = document.id,
                fileId = document.file.id,
                originalFilename = document.file.originalFilename,
                mimeType = document.file.mimeType,
                fileSize = document.file.fileSize,
            )
    }
}
