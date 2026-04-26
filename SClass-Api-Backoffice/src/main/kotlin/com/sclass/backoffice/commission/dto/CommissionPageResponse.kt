package com.sclass.backoffice.commission.dto

import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.commission.dto.CommissionWithDetailDto
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import java.time.LocalDateTime

data class CommissionPageResponse(
    val content: List<CommissionListResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)

data class CommissionListResponse(
    val id: Long,
    val studentUserId: String,
    val studentName: String,
    val teacherUserId: String,
    val teacherName: String,
    val commissionPolicyId: String,
    val outputFormat: OutputFormat,
    val activityType: ActivityType,
    val status: CommissionListStatus,
    val guideSubject: String,
    val createdAt: LocalDateTime,
    val lesson: LessonSummary?,
) {
    data class LessonSummary(
        val id: Long,
        val name: String,
        val status: LessonStatus,
        val scheduledAt: LocalDateTime?,
        val startedAt: LocalDateTime?,
        val completedAt: LocalDateTime?,
    ) {
        companion object {
            fun from(lesson: Lesson) =
                LessonSummary(
                    id = lesson.id,
                    name = lesson.name,
                    status = lesson.status,
                    scheduledAt = lesson.scheduledAt,
                    startedAt = lesson.startedAt,
                    completedAt = lesson.completedAt,
                )
        }
    }

    companion object {
        fun from(dto: CommissionWithDetailDto) =
            CommissionListResponse(
                id = dto.commission.id,
                studentUserId = dto.commission.studentUserId,
                studentName = dto.studentName,
                teacherUserId = dto.commission.teacherUserId,
                teacherName = dto.teacherName,
                commissionPolicyId = dto.commission.commissionPolicyId,
                outputFormat = dto.commission.outputFormat,
                activityType = dto.commission.activityType,
                status = CommissionListStatus.from(dto),
                guideSubject = dto.commission.guideInfo.subject,
                createdAt = dto.commission.createdAt,
                lesson = dto.lesson?.let { LessonSummary.from(it) },
            )
    }
}

enum class CommissionListStatus {
    REQUESTED,
    TOPIC_PROPOSED,
    ACCEPTED,
    IN_PROGRESS,
    REJECTED,
    CANCELLED,
    COMPLETED,
    ;

    companion object {
        fun from(dto: CommissionWithDetailDto): CommissionListStatus {
            when (dto.lesson?.status) {
                LessonStatus.IN_PROGRESS -> return IN_PROGRESS
                LessonStatus.COMPLETED -> return COMPLETED
                else -> Unit
            }
            return when (dto.commission.status) {
                CommissionStatus.REQUESTED -> REQUESTED
                CommissionStatus.TOPIC_PROPOSED -> TOPIC_PROPOSED
                CommissionStatus.ACCEPTED -> ACCEPTED
                CommissionStatus.REJECTED -> REJECTED
                CommissionStatus.CANCELLED -> CANCELLED
            }
        }
    }
}
