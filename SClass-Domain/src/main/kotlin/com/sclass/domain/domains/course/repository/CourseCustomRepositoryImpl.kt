package com.sclass.domain.domains.course.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.domain.QCourse.course
import com.sclass.domain.domains.course.dto.CourseWithTeacherAndEnrollmentCountDto
import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
import com.sclass.domain.domains.enrollment.domain.QEnrollment.enrollment
import com.sclass.domain.domains.teacher.domain.QTeacher.teacher
import com.sclass.domain.domains.user.domain.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class CourseCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CourseCustomRepository {
    override fun findAllActiveWithTeacher(): List<CourseWithTeacherDto> =
        queryFactory
            .select(course, teacher, user)
            .from(course)
            .leftJoin(teacher)
            .on(teacher.user.id.eq(course.teacherUserId))
            .leftJoin(user)
            .on(user.id.eq(course.teacherUserId))
            .where(course.status.eq(CourseStatus.ACTIVE))
            .fetch()
            .map { tuple ->
                CourseWithTeacherDto(
                    course = tuple[course]!!,
                    teacher = tuple[teacher],
                    teacherUser = tuple[user],
                )
            }

    override fun searchCourses(
        teacherUserId: String?,
        status: CourseStatus?,
        pageable: Pageable,
    ): Page<CourseWithTeacherAndEnrollmentCountDto> {
        val where = mutableListOf<BooleanExpression>()
        teacherUserId?.let { where += course.teacherUserId.eq(it) }
        status?.let { where += course.status.eq(it) }

        val content =
            queryFactory
                .select(course, user.name, enrollment.count())
                .from(course)
                .leftJoin(user)
                .on(user.id.eq(course.teacherUserId))
                .leftJoin(enrollment)
                .on(enrollment.courseId.eq(course.id))
                .where(*where.toTypedArray())
                .groupBy(course.id)
                .orderBy(course.createdAt.desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
                .map { tuple ->
                    CourseWithTeacherAndEnrollmentCountDto(
                        course = tuple[course]!!,
                        teacherName = tuple[user.name] ?: "-",
                        enrollmentCount = tuple[enrollment.count()] ?: 0L,
                    )
                }

        val total =
            queryFactory
                .select(course.count())
                .from(course)
                .where(*where.toTypedArray())
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }
}
