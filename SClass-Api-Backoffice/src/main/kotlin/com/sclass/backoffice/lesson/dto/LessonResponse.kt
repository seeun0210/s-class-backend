package com.sclass.backoffice.lesson.dto

import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import java.time.LocalDateTime

data class LessonResponse(
    val id: Long,
    val name: String,
    val lessonType: LessonType,
    val enrollmentId: Long?,
    val sourceCommissionId: Long?,
    val studentUserId: String,
    val assignedTeacherUserId: String,
    val substituteTeacherUserId: String?,
    val actualTeacherUserId: String?,
    val status: LessonStatus,
    val scheduledAt: LocalDateTime?,
    val startedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
) {
    companion object {
        fun from(lesson: Lesson) =
            LessonResponse(
                id = lesson.id,
                name = lesson.name,
                lessonType = lesson.lessonType,
                enrollmentId = lesson.enrollmentId,
                sourceCommissionId = lesson.sourceCommissionId,
                studentUserId = lesson.studentUserId,
                assignedTeacherUserId = lesson.assignedTeacherUserId,
                substituteTeacherUserId = lesson.substituteTeacherUserId,
                actualTeacherUserId = lesson.actualTeacherUserId,
                status = lesson.status,
                scheduledAt = lesson.scheduledAt,
                startedAt = lesson.startedAt,
                completedAt = lesson.completedAt,
            )
    }
}
