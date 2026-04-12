package com.sclass.domain.domains.enrollment.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.course.domain.QCourse.course
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.QEnrollment.enrollment
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithDetailDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto
import com.sclass.domain.domains.user.domain.QUser
import com.sclass.domain.domains.user.domain.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

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

    override fun searchEnrollments(
        studentUserId: String?,
        courseId: Long?,
        status: EnrollmentStatus?,
        pageable: Pageable,
    ): Page<EnrollmentWithDetailDto> {
        val where = mutableListOf<BooleanExpression>()
        studentUserId?.let { where += enrollment.studentUserId.eq(it) }
        courseId?.let { where += enrollment.courseId.eq(it) }
        status?.let { where += enrollment.status.eq(it) }

        val studentUser = QUser("studentUser")
        val teacherUser = QUser("teacherUser")

        val content =
            queryFactory
                .select(enrollment, studentUser.name, course.name, teacherUser.name)
                .from(enrollment)
                .join(course)
                .on(course.id.eq(enrollment.courseId))
                .leftJoin(studentUser)
                .on(studentUser.id.eq(enrollment.studentUserId))
                .leftJoin(teacherUser)
                .on(teacherUser.id.eq(course.teacherUserId))
                .where(*where.toTypedArray())
                .orderBy(enrollment.createdAt.desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
                .map { tuple ->
                    EnrollmentWithDetailDto(
                        enrollment = tuple[enrollment]!!,
                        studentName = tuple[studentUser.name] ?: "",
                        courseName = tuple[course.name] ?: "",
                        teacherName = tuple[teacherUser.name] ?: "",
                    )
                }

        val total =
            queryFactory
                .select(enrollment.count())
                .from(enrollment)
                .join(course)
                .on(course.id.eq(enrollment.courseId))
                .where(*where.toTypedArray())
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }
}
