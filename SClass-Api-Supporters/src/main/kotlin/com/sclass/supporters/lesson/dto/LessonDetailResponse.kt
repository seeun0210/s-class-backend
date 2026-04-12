package com.sclass.supporters.lesson.dto

import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import java.time.LocalDateTime

data class LessonDetailResponse(
    val id: Long,
    val name: String,
    val lessonNumber: Int?,
    val lessonType: LessonType,
    val enrollmentId: Long?,
    val sourceCommissionId: Long?,
    val status: LessonStatus,
    val scheduledAt: LocalDateTime?,
    val startedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val inquiryPlanId: Long?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            lesson: Lesson,
            inquiryPlanId: Long?,
        ) = LessonDetailResponse(
            id = lesson.id,
            name = lesson.name,
            lessonNumber = lesson.lessonNumber,
            lessonType = lesson.lessonType,
            enrollmentId = lesson.enrollmentId,
            sourceCommissionId = lesson.sourceCommissionId,
            status = lesson.status,
            scheduledAt = lesson.scheduledAt,
            startedAt = lesson.startedAt,
            completedAt = lesson.completedAt,
            inquiryPlanId = inquiryPlanId,
            createdAt = lesson.createdAt,
        )
    }
}
