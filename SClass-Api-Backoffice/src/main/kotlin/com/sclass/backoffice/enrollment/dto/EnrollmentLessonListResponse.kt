package com.sclass.backoffice.enrollment.dto

import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import java.time.LocalDateTime

data class EnrollmentLessonListResponse(
    val lessons: List<EnrollmentLessonResponse>,
)

data class EnrollmentLessonResponse(
    val id: Long,
    val lessonType: LessonType,
    val lessonNumber: Int?,
    val name: String,
    val status: LessonStatus,
    val scheduledAt: LocalDateTime?,
    val startedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val teacherPayoutAmountWon: Int,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(lesson: Lesson) =
            EnrollmentLessonResponse(
                id = lesson.id,
                lessonType = lesson.lessonType,
                lessonNumber = lesson.lessonNumber,
                name = lesson.name,
                status = lesson.status,
                scheduledAt = lesson.scheduledAt,
                startedAt = lesson.startedAt,
                completedAt = lesson.completedAt,
                teacherPayoutAmountWon = lesson.teacherPayoutAmountWon,
                createdAt = lesson.createdAt,
            )
    }
}
