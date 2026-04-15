package com.sclass.domain.domains.lesson.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.QLesson

class LessonCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : LessonCustomRepository {
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
}
