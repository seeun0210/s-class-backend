package com.sclass.backoffice.commission.dto

import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.CommissionTopic
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.file.domain.FileType
import java.time.LocalDateTime

data class CommissionDetailResponse(
    val id: Long,
    val studentUserId: String,
    val teacherUserId: String,
    val commissionPolicyId: String,
    val outputFormat: OutputFormat,
    val activityType: ActivityType,
    val status: CommissionStatus,
    val guideInfo: CommissionGuideInfoResponse,
    val selectedTopic: CommissionTopicResponse?,
    val topics: List<CommissionTopicResponse>,
    val files: List<CommissionFileResponse>,
    val acceptedLessonId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(
            commission: Commission,
            topics: List<CommissionTopic>,
            files: List<CommissionFile>,
        ) = CommissionDetailResponse(
            id = commission.id,
            studentUserId = commission.studentUserId,
            teacherUserId = commission.teacherUserId,
            commissionPolicyId = commission.commissionPolicyId,
            outputFormat = commission.outputFormat,
            activityType = commission.activityType,
            status = commission.status,
            guideInfo = CommissionGuideInfoResponse.from(commission),
            selectedTopic =
                topics
                    .firstOrNull { it.id == commission.selectedTopicId || it.selected }
                    ?.let { CommissionTopicResponse.from(it) },
            topics = topics.map { CommissionTopicResponse.from(it) },
            files = files.map { CommissionFileResponse.from(it) },
            acceptedLessonId = commission.acceptedLessonId,
            createdAt = commission.createdAt,
            updatedAt = commission.updatedAt,
        )
    }
}

data class CommissionGuideInfoResponse(
    val subject: String,
    val volume: String,
    val requiredElements: String?,
    val gradingCriteria: String,
    val teacherEmphasis: String,
) {
    companion object {
        fun from(commission: Commission) =
            with(commission.guideInfo) {
                CommissionGuideInfoResponse(
                    subject = subject,
                    volume = volume,
                    requiredElements = requiredElements,
                    gradingCriteria = gradingCriteria,
                    teacherEmphasis = teacherEmphasis,
                )
            }
    }
}

data class CommissionTopicResponse(
    val id: Long,
    val topicId: String?,
    val title: String,
    val description: String?,
    val selected: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(topic: CommissionTopic) =
            CommissionTopicResponse(
                id = topic.id,
                topicId = topic.topicId,
                title = topic.title,
                description = topic.description,
                selected = topic.selected,
                createdAt = topic.createdAt,
            )
    }
}

data class CommissionFileResponse(
    val commissionFileId: Long,
    val fileId: String,
    val originalFilename: String,
    val mimeType: String,
    val fileSize: Long,
    val fileType: FileType,
    val uploadedBy: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(commissionFile: CommissionFile) =
            CommissionFileResponse(
                commissionFileId = commissionFile.id,
                fileId = commissionFile.file.id,
                originalFilename = commissionFile.file.originalFilename,
                mimeType = commissionFile.file.mimeType,
                fileSize = commissionFile.file.fileSize,
                fileType = commissionFile.file.fileType,
                uploadedBy = commissionFile.file.uploadedBy,
                createdAt = commissionFile.file.createdAt,
            )
    }
}
