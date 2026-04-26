package com.sclass.domain.domains.lesson.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.QLesson
import jakarta.persistence.LockModeType
import java.time.LocalDateTime

class LessonCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : LessonCustomRepository {
    override fun findByIdForUpdate(id: Long): Lesson? =
        queryFactory
            .selectFrom(QLesson.lesson)
            .where(QLesson.lesson.id.eq(id))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne()

    override fun findAllByEffectiveTeacher(teacherUserId: String): List<Lesson> =
        queryFactory
            .selectFrom(QLesson.lesson)
            .where(
                QLesson.lesson.substituteTeacherUserId
                    .eq(teacherUserId)
                    .or(
                        QLesson.lesson.substituteTeacherUserId.isNull.and(
                            QLesson.lesson.assignedTeacherUserId.eq(
                                teacherUserId,
                            ),
                        ),
                    ),
            ).fetch()

    override fun existsScheduleConflict(
        studentUserId: String,
        teacherUserId: String,
        scheduledAt: LocalDateTime,
        requestedDurationMinutes: Long,
        excludeLessonId: Long,
    ): Boolean {
        val lesson = QLesson.lesson
        val existingLessonDurationMinutes = Lesson.DEFAULT_DURATION_MINUTES
        val conflictStartAt = scheduledAt.minusMinutes(existingLessonDurationMinutes)
        val conflictEndAt = scheduledAt.plusMinutes(requestedDurationMinutes)

        return queryFactory
            .selectOne()
            .from(lesson)
            .where(
                lesson.id.ne(excludeLessonId),
                lesson.status.`in`(SCHEDULE_CONFLICT_TARGET_STATUSES),
                lesson.scheduledAt.gt(conflictStartAt),
                lesson.scheduledAt.lt(conflictEndAt),
                lesson.studentUserId
                    .eq(studentUserId)
                    .or(lesson.effectiveTeacherEq(teacherUserId)),
            ).fetchFirst() != null
    }

    private fun QLesson.effectiveTeacherEq(teacherUserId: String): BooleanExpression =
        substituteTeacherUserId
            .eq(teacherUserId)
            .or(
                substituteTeacherUserId.isNull.and(
                    assignedTeacherUserId.eq(teacherUserId),
                ),
            )

    private companion object {
        val SCHEDULE_CONFLICT_TARGET_STATUSES = listOf(LessonStatus.SCHEDULED, LessonStatus.IN_PROGRESS)
    }
}
