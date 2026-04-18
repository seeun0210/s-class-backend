package com.sclass.domain.domains.course.repository

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections.tuple
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.domain.QCourse.course
import com.sclass.domain.domains.course.dto.CatalogCourseDto
import com.sclass.domain.domains.course.dto.CourseWithEnrollmentCountDto
import com.sclass.domain.domains.course.dto.CourseWithTeacherAndEnrollmentCountDto
import com.sclass.domain.domains.course.dto.CourseWithTeacherDto
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.QEnrollment.enrollment
import com.sclass.domain.domains.product.domain.QCourseProduct.courseProduct
import com.sclass.domain.domains.teacher.domain.QTeacher.teacher
import com.sclass.domain.domains.user.domain.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.LocalDateTime

class CourseCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CourseCustomRepository {
    override fun findAllByTeacherUserIdWithEnrollmentCount(teacherUserId: String): List<CourseWithEnrollmentCountDto> =
        queryFactory
            .select(course, courseProduct, enrollment.count())
            .from(course)
            .leftJoin(courseProduct)
            .on(courseProduct.id.eq(course.productId))
            .leftJoin(enrollment)
            .on(enrollment.courseId.eq(course.id))
            .where(course.teacherUserId.eq(teacherUserId))
            .groupBy(course.id, courseProduct.id)
            .fetch()
            .map { tuple ->
                CourseWithEnrollmentCountDto(
                    course = tuple[course]!!,
                    courseProduct = tuple[courseProduct],
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
                    courseProduct,
                    user.name,
                    enrollment.count(),
                ).from(course)
                .leftJoin(courseProduct)
                .on(courseProduct.id.eq(course.productId))
                .leftJoin(user)
                .on(user.id.eq(course.teacherUserId))
                .leftJoin(enrollment)
                .on(enrollment.courseId.eq(course.id))
                .where(*where.toTypedArray())
                .groupBy(course, courseProduct, user.name)
                .orderBy(*pageable.sort.toOrderSpecifiers())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
                .map { tuple ->
                    CourseWithTeacherAndEnrollmentCountDto(
                        course = tuple[course]!!,
                        courseProduct = tuple[courseProduct],
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

    override fun findAllCatalogCourses(pageable: Pageable): Page<CatalogCourseDto> {
        val now = LocalDateTime.now()
        val deadlineNotPassed = course.enrollmentDeadLine.isNull.or(course.enrollmentDeadLine.goe(now))
        val content =
            queryFactory
                .select(course, courseProduct, teacher, user, enrollment.count())
                .from(course)
                .leftJoin(courseProduct)
                .on(courseProduct.id.eq(course.productId))
                .leftJoin(teacher)
                .on(teacher.user.id.eq(course.teacherUserId))
                .leftJoin(user)
                .on(user.id.eq(course.teacherUserId))
                .leftJoin(enrollment)
                .on(
                    enrollment.courseId.eq(course.id),
                    enrollment.status.`in`(EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.ACTIVE),
                ).where(
                    course.status.eq(CourseStatus.LISTED),
                    courseProduct.visible.isTrue,
                    deadlineNotPassed,
                ).groupBy(course, courseProduct, teacher, user)
                .orderBy(*pageable.sort.toOrderSpecifiers())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
                .map { tuple ->
                    CatalogCourseDto(
                        course = tuple[course]!!,
                        courseProduct = tuple[courseProduct],
                        teacher = tuple[teacher],
                        teacherUser = tuple[user],
                        liveEnrollmentCount = tuple[enrollment.count()] ?: 0L,
                    )
                }

        val total =
            queryFactory
                .select(course.count())
                .from(course)
                .leftJoin(courseProduct)
                .on(courseProduct.id.eq(course.productId))
                .where(
                    course.status.eq(CourseStatus.LISTED),
                    courseProduct.visible.isTrue,
                    deadlineNotPassed,
                ).fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun findCatalogCourseById(id: Long): CourseWithTeacherDto? =
        queryFactory
            .select(course, courseProduct, teacher, user)
            .from(course)
            .leftJoin(courseProduct)
            .on(courseProduct.id.eq(course.productId))
            .leftJoin(teacher)
            .on(teacher.user.id.eq(course.teacherUserId))
            .leftJoin(user)
            .on(user.id.eq(course.teacherUserId))
            .where(
                course.id.eq(id),
                course.status.eq(CourseStatus.LISTED),
                courseProduct.visible.isTrue,
            ).fetchOne()
            ?.let { tuple ->
                CourseWithTeacherDto(
                    course = tuple[course]!!,
                    courseProduct = tuple[courseProduct],
                    teacher = tuple[teacher],
                    teacherUser = tuple[user],
                )
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
