package com.sclass.domain.domains.course.repository

import com.querydsl.core.types.Projections.tuple
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.domain.QCourse.course
import com.sclass.domain.domains.course.dto.CourseWithEnrollmentCountDto
import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
import com.sclass.domain.domains.enrollment.domain.QEnrollment.enrollment
import com.sclass.domain.domains.teacher.domain.QTeacher.teacher
import com.sclass.domain.domains.user.domain.QUser.user

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

    override fun findAllByTeacherUserIdWithEnrollmentCount(teacherUserId: String): List<CourseWithEnrollmentCountDto> =
        queryFactory
            .select(course, enrollment.count())
            .from(course)
            .leftJoin(enrollment)
            .on(enrollment.courseId.eq(course.id))
            .where(course.teacherUserId.eq(teacherUserId))
            .groupBy(course.id)
            .fetch()
            .map { tuple ->
                CourseWithEnrollmentCountDto(
                    course = tuple[course]!!,
                    enrollmentCount = tuple[enrollment.count()] ?: 0L,
                )
            }
}
