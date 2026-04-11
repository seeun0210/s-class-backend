package com.sclass.domain.domains.course.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.domain.QCourse.course
import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
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
}
