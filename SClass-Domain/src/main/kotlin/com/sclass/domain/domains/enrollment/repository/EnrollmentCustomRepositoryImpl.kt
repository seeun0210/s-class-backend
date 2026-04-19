package com.sclass.domain.domains.enrollment.repository

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.course.domain.QCourse.course
import com.sclass.domain.domains.enrollment.domain.Enrollment
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.QEnrollment.enrollment
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithCourseDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithDetailDto
import com.sclass.domain.domains.enrollment.dto.EnrollmentWithStudentDto
import com.sclass.domain.domains.enrollment.dto.ProductEnrollmentCountDto
import com.sclass.domain.domains.product.domain.QCourseProduct.courseProduct
import com.sclass.domain.domains.product.domain.QMembershipProduct.membershipProduct
import com.sclass.domain.domains.user.domain.QUser
import com.sclass.domain.domains.user.domain.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.LocalDateTime

class EnrollmentCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : EnrollmentCustomRepository {
    override fun findAllByStudentUserIdWithCourse(studentUserId: String): List<EnrollmentWithCourseDto> =
        queryFactory
            .select(enrollment, course, courseProduct, user.name, membershipProduct)
            .from(enrollment)
            .leftJoin(course)
            .on(course.id.eq(enrollment.courseId))
            .leftJoin(courseProduct)
            .on(courseProduct.id.eq(course.productId))
            .leftJoin(user)
            .on(user.id.eq(course.teacherUserId))
            .leftJoin(membershipProduct)
            .on(membershipProduct.id.eq(enrollment.productId))
            .where(enrollment.studentUserId.eq(studentUserId))
            .orderBy(enrollment.createdAt.desc())
            .fetch()
            .map { tuple ->
                EnrollmentWithCourseDto(
                    enrollment = tuple[enrollment]!!,
                    course = tuple[course],
                    courseProduct = tuple[courseProduct],
                    teacherName = tuple[user.name],
                    membershipProduct = tuple[membershipProduct],
                )
            }

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
        teacherUserId: String?,
        courseId: Long?,
        status: EnrollmentStatus?,
        pageable: Pageable,
    ): Page<EnrollmentWithDetailDto> {
        val where = mutableListOf<BooleanExpression>()
        studentUserId?.let { where += enrollment.studentUserId.eq(it) }
        teacherUserId?.let { where += course.teacherUserId.eq(it) }
        courseId?.let { where += enrollment.courseId.eq(it) }
        status?.let { where += enrollment.status.eq(it) }

        val studentUser = QUser("studentUser")
        val teacherUser = QUser("teacherUser")

        val content =
            queryFactory
                .select(enrollment, studentUser.name, courseProduct.name, course.teacherUserId, teacherUser.name)
                .from(enrollment)
                .leftJoin(course)
                .on(course.id.eq(enrollment.courseId))
                .leftJoin(courseProduct)
                .on(courseProduct.id.eq(course.productId))
                .leftJoin(studentUser)
                .on(studentUser.id.eq(enrollment.studentUserId))
                .leftJoin(teacherUser)
                .on(teacherUser.id.eq(course.teacherUserId))
                .where(*where.toTypedArray())
                .orderBy(*pageable.sort.toOrderSpecifiers())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
                .map { tuple ->
                    EnrollmentWithDetailDto(
                        enrollment = tuple[enrollment]!!,
                        studentName = tuple[studentUser.name] ?: "",
                        courseName = tuple[courseProduct.name] ?: "",
                        teacherUserId = tuple[course.teacherUserId] ?: "",
                        teacherName = tuple[teacherUser.name] ?: "",
                    )
                }

        val total =
            queryFactory
                .select(enrollment.count())
                .from(enrollment)
                .leftJoin(course)
                .on(course.id.eq(enrollment.courseId))
                .where(*where.toTypedArray())
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun countLiveMembershipEnrollmentsByProductIds(productIds: Collection<String>): List<ProductEnrollmentCountDto> {
        if (productIds.isEmpty()) return emptyList()
        return queryFactory
            .select(enrollment.productId, enrollment.count())
            .from(enrollment)
            .where(
                enrollment.productId.`in`(productIds),
                enrollment.status.`in`(EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.ACTIVE),
            ).groupBy(enrollment.productId)
            .fetch()
            .mapNotNull { tuple ->
                val productId = tuple[enrollment.productId] ?: return@mapNotNull null
                ProductEnrollmentCountDto(productId = productId, count = tuple[enrollment.count()] ?: 0L)
            }
    }

    override fun hasActiveMembershipEnrollment(
        studentUserId: String,
        now: LocalDateTime,
    ): Boolean =
        queryFactory
            .selectOne()
            .from(enrollment)
            .where(
                enrollment.studentUserId.eq(studentUserId),
                enrollment.productId.isNotNull,
                enrollment.status.eq(EnrollmentStatus.ACTIVE),
                enrollment.endAt.isNull.or(enrollment.endAt.gt(now)),
            ).fetchFirst() != null

    private fun Sort.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        if (isUnsorted) return arrayOf(enrollment.createdAt.desc())
        val path = PathBuilder(Enrollment::class.java, "enrollment")
        return map { order ->
            val direction = if (order.isAscending) Order.ASC else Order.DESC
            OrderSpecifier(direction, path.get(order.property, Comparable::class.java))
        }.toList().toTypedArray()
    }
}
