package com.sclass.domain.domains.enrollment.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.enrollment.domain.QEnrollment.enrollment
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto
import com.sclass.domain.domains.user.domain.QUser.user

class EnrollmentCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : EnrollmentCustomRepository {
    override fun findAllByCourseIdWithStudent(courseId: Long): List<EnrollmentWithStudentDto> =
        queryFactory
            .select(enrollment, user)
            .from(enrollment)
            .leftJoin(user)
            .on(user.id.eq(enrollment.studentUserId))
            .where(enrollment.courseId.eq(courseId))
            .fetch()
            .map { tuple ->
                EnrollmentWithStudentDto(
                    enrollment = tuple[enrollment]!!,
                    student = tuple[user],
                )
            }
}
