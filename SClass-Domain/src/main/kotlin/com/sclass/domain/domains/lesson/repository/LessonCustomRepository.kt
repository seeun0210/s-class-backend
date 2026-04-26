package com.sclass.domain.domains.lesson.repository

import com.sclass.domain.domains.lesson.domain.Lesson
import java.time.LocalDateTime

interface LessonCustomRepository {
    fun findAllByEffectiveTeacher(teacherUserId: String): List<Lesson>

    fun existsScheduleConflict(
        studentUserId: String,
        teacherUserId: String,
        scheduledAt: LocalDateTime,
        durationMinutes: Long,
        excludeLessonId: Long,
    ): Boolean
}
