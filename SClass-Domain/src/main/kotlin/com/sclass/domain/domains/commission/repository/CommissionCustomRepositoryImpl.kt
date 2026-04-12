package com.sclass.domain.domains.commission.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.QCommission.commission
import com.sclass.domain.domains.commission.dto.CommissionWithDetailDto
import com.sclass.domain.domains.lesson.domain.QLesson.lesson
import com.sclass.domain.domains.user.domain.QUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class CommissionCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CommissionCustomRepository {
    override fun searchCommissions(
        studentUserId: String?,
        teacherUserId: String?,
        status: CommissionStatus?,
        pageable: Pageable,
    ): Page<CommissionWithDetailDto> {
        val where = mutableListOf<BooleanExpression>()
        studentUserId?.let { where += commission.studentUserId.eq(it) }
        teacherUserId?.let { where += commission.teacherUserId.eq(it) }
        status?.let { where += commission.status.eq(it) }

        val studentUser = QUser("studentUser")
        val teacherUser = QUser("teacherUser")

        val content =
            queryFactory
                .select(commission, studentUser.name, teacherUser.name, lesson)
                .from(commission)
                .leftJoin(studentUser)
                .on(studentUser.id.eq(commission.studentUserId))
                .leftJoin(teacherUser)
                .on(teacherUser.id.eq(commission.teacherUserId))
                .leftJoin(lesson)
                .on(lesson.id.eq(commission.acceptedLessonId))
                .where(*where.toTypedArray())
                .orderBy(commission.createdAt.desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
                .map { tuple ->
                    CommissionWithDetailDto(
                        commission = tuple[commission]!!,
                        studentName = tuple[studentUser.name] ?: "",
                        teacherName = tuple[teacherUser.name] ?: "",
                        lesson = tuple[lesson],
                    )
                }

        val total =
            queryFactory
                .select(commission.count())
                .from(commission)
                .where(*where.toTypedArray())
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }
}
