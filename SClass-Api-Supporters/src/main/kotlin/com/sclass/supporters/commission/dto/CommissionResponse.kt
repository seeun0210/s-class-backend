package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.OutputFormat
import java.time.LocalDateTime

data class CommissionResponse(
    val id: Long,
    val studentUserId: String,
    val teacherUserId: String,
    val outputFormat: OutputFormat,
    val activityType: ActivityType,
    val status: CommissionStatus,
    val guideInfo: GuideInfoResponse,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(commission: Commission): CommissionResponse =
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
