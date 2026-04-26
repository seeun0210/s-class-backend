package com.sclass.domain.domains.lesson.repository

import com.sclass.domain.domains.lesson.domain.Lesson
import java.time.LocalDateTime

interface LessonCustomRepository {
    fun findByIdForUpdate(id: Long): Lesson?

    fun findAllByEffectiveTeacher(teacherUserId: String): List<Lesson>

    fun existsScheduleConflict(
        studentUserId: String,
        teacherUserId: String,
        scheduledAt: LocalDateTime,
        requestedDurationMinutes: Long,
        excludeLessonId: Long,
    ): Boolean
}
