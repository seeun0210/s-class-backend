package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import java.time.LocalDateTime

data class CommissionListResponse(
    val commissions: List<CommissionSummary>,
) {
    companion object {
        fun from(
            commissions: List<Commission>,
            lessonsById: Map<Long, Lesson> = emptyMap(),
        ): CommissionListResponse =
            CommissionListResponse(
                commissions =
                    commissions.map { commission ->
                        CommissionSummary.from(
                            commission = commission,
                            lesson = commission.acceptedLessonId?.let { lessonsById[it] },
                        )
                    },
            )
    }
}

data class CommissionSummary(
    val id: Long,
    val outputFormat: OutputFormat,
    val activityType: ActivityType,
    val status: CommissionSummaryStatus,
    val subject: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            commission: Commission,
            lesson: Lesson? = null,
        ): CommissionSummary =
            CommissionSummary(
                id = commission.id,
                outputFormat = commission.outputFormat,
                activityType = commission.activityType,
                status = CommissionSummaryStatus.from(commission, lesson),
                subject = commission.guideInfo.subject,
                createdAt = commission.createdAt,
            )
    }
}

enum class CommissionSummaryStatus {
    REQUESTED,
    TOPIC_PROPOSED,
    ACCEPTED,
    IN_PROGRESS,
    REJECTED,
    CANCELLED,
    COMPLETED,
    ;

    companion object {
        fun from(
            commission: Commission,
            lesson: Lesson?,
        ): CommissionSummaryStatus {
            when (lesson?.status) {
                LessonStatus.IN_PROGRESS -> return IN_PROGRESS
                LessonStatus.COMPLETED -> return COMPLETED
                else -> Unit
            }
            return when (commission.status) {
                CommissionStatus.REQUESTED -> REQUESTED
                CommissionStatus.TOPIC_PROPOSED -> TOPIC_PROPOSED
                CommissionStatus.ACCEPTED -> ACCEPTED
                CommissionStatus.REJECTED -> REJECTED
                CommissionStatus.CANCELLED -> CANCELLED
            }
        }
    }
}
