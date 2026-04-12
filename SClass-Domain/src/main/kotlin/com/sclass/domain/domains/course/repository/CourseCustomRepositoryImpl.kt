package com.sclass.domain.domains.course.repository

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.domain.QCourse.course
import com.sclass.domain.domains.course.dto.CourseWithEnrollmentCountDto
import com.sclass.domain.domains.course.dto.CourseWithTeacherAndEnrollmentCountDto
import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
import com.sclass.domain.domains.enrollment.domain.QEnrollment.enrollment
import com.sclass.domain.domains.product.domain.QCourseProduct.courseProduct
import com.sclass.domain.domains.teacher.domain.QTeacher.teacher
import com.sclass.domain.domains.user.domain.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

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
                .select(
                    course,
                    user.name,
                    enrollment.count(),
                    courseProduct.totalLessons,
                ).from(course)
                .leftJoin(user)
                .on(user.id.eq(course.teacherUserId))
                .leftJoin(enrollment)
                .on(enrollment.courseId.eq(course.id))
                .leftJoin(courseProduct)
                .on(courseProduct.id.eq(course.productId))
                .where(*where.toTypedArray())
                .groupBy(course, user.name, courseProduct.totalLessons)
                .orderBy(*pageable.sort.toOrderSpecifiers())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
                .map { tuple ->
                    CourseWithTeacherAndEnrollmentCountDto(
                        course = tuple[course]!!,
                        teacherName = tuple[user.name] ?: "-",
                        enrollmentCount = tuple[enrollment.count()] ?: 0L,
                        totalLessons = tuple[courseProduct.totalLessons] ?: 0,
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

    private fun Sort.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        if (isUnsorted) return arrayOf(course.createdAt.desc())
        val coursePath = PathBuilder(Course::class.java, "course")
        return mapNotNull { order ->
            val direction = if (order.isAscending) Order.ASC else Order.DESC
            when (order.property) {
                "totalLessons" -> OrderSpecifier(direction, courseProduct.totalLessons)
                else -> OrderSpecifier(direction, coursePath.get(order.property, Comparable::class.java))
            }
        }.toTypedArray()
    }
}
