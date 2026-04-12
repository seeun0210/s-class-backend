package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.file.domain.FileType
import java.time.LocalDateTime

data class CommissionResponse(
    val id: Long,
    val studentUserId: String,
    val teacherUserId: String,
    val outputFormat: OutputFormat,
    val activityType: ActivityType,
    val status: CommissionStatus,
    val guideInfo: GuideInfoResponse,
    val commissionFiles: List<CommissionFileResponse>,
    val lessonId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(
            commission: Commission,
            commissionFiles: List<CommissionFile> = emptyList(),
        ): CommissionResponse =
            CommissionResponse(
                id = commission.id,
                studentUserId = commission.studentUserId,
                teacherUserId = commission.teacherUserId,
                outputFormat = commission.outputFormat,
                activityType = commission.activityType,
                status = commission.status,
                guideInfo =
                    GuideInfoResponse(
                        subject = commission.guideInfo.subject,
                        volume = commission.guideInfo.volume,
                        requiredElements = commission.guideInfo.requiredElements,
                        gradingCriteria = commission.guideInfo.gradingCriteria,
                        teacherEmphasis = commission.guideInfo.teacherEmphasis,
                    ),
                commissionFiles = commissionFiles.map { CommissionFileResponse.from(it) },
                lessonId = commission.acceptedLessonId,
                createdAt = commission.createdAt,
                updatedAt = commission.updatedAt,
            )
    }
}

data class GuideInfoResponse(
    val subject: String,
    val volume: String,
    val requiredElements: String?,
    val gradingCriteria: String,
    val teacherEmphasis: String,
)

data class CommissionFileResponse(
    val id: Long,
    val fileMeta: FileMetaResponse,
) {
    companion object {
        fun from(commissionFile: CommissionFile): CommissionFileResponse =
            CommissionFileResponse(
                id = commissionFile.id,
                fileMeta =
                    FileMetaResponse(
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

data class FileMetaResponse(
    val id: String,
    val originalFilename: String,
    val mimeType: String,
    val fileSize: Long,
    val fileType: FileType,
    val uploadedBy: String,
    val createdAt: LocalDateTime,
)
