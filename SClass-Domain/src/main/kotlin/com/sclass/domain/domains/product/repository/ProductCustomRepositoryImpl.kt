package com.sclass.domain.domains.product.repository

import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.coin.domain.QCoinPackage
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.domain.QCourse.course
import com.sclass.domain.domains.enrollment.domain.EnrollmentStatus
import com.sclass.domain.domains.enrollment.domain.QEnrollment.enrollment
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductCatalogSort
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.QCourseProduct
import com.sclass.domain.domains.product.domain.QMembershipProduct
import com.sclass.domain.domains.product.domain.QProduct
import com.sclass.domain.domains.product.dto.MembershipProductWithCoinPackageDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.LocalDateTime

class ProductCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : ProductCustomRepository {
    override fun findAllActiveByType(type: ProductType?): List<Product> =
        queryFactory
            .selectFrom(QProduct.product)
            .where(
                QProduct.product.visible.isTrue,
                type?.let { QProduct.product.instanceOf(it.entityClass) },
            ).fetch()

    override fun findMembershipsWithCoinPackage(
        type: ProductType?,
        visibleOnly: Boolean,
        pageable: Pageable,
    ): Page<MembershipProductWithCoinPackageDto> {
        val qMembership = QMembershipProduct.membershipProduct
        val qCoin = QCoinPackage.coinPackage
        val conditions =
            listOfNotNull(
                if (visibleOnly) qMembership.visible.isTrue else null,
                type?.let {
                    require(it.entityClass != null && MembershipProduct::class.java.isAssignableFrom(it.entityClass)) {
                        "ProductType $it is not a subtype of MembershipProduct"
                    }
                    @Suppress("UNCHECKED_CAST")
                    qMembership.instanceOf(it.entityClass as Class<out MembershipProduct>)
                },
            )
        val whereArr = conditions.toTypedArray()

        val content =
            queryFactory
                .select(qMembership, qCoin)
                .from(qMembership)
                .leftJoin(qCoin)
                .on(qMembership.coinPackageId.eq(qCoin.id))
                .where(*whereArr)
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .orderBy(qMembership.createdAt.desc())
                .fetch()
                .map { tuple ->
                    MembershipProductWithCoinPackageDto(
                        product = tuple[qMembership]!!,
                        coinPackage = tuple[qCoin],
                    )
                }
        val total =
            queryFactory
                .select(qMembership.count())
                .from(qMembership)
                .where(*whereArr)
                .fetchOne() ?: 0L
        return PageImpl(content, pageable, total)
    }

    override fun findCourseProducts(pageable: Pageable): Page<CourseProduct> {
        val qCourseProduct = QCourseProduct.courseProduct

        val content =
            queryFactory
                .selectFrom(qCourseProduct)
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .orderBy(*pageable.sort.toCourseProductOrderSpecifiers())
                .fetch()

        val total =
            queryFactory
                .select(qCourseProduct.count())
                .from(qCourseProduct)
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    private fun Sort.toCourseProductOrderSpecifiers(): Array<OrderSpecifier<*>> {
        if (isUnsorted) return arrayOf(QCourseProduct.courseProduct.createdAt.desc(), QCourseProduct.courseProduct.id.asc())

        val path = PathBuilder(CourseProduct::class.java, "courseProduct")
        val orderSpecifiers: MutableList<OrderSpecifier<*>> =
            map { order ->
                val direction = if (order.isAscending) Order.ASC else Order.DESC
                OrderSpecifier(direction, path.get(order.property, Comparable::class.java))
            }.toMutableList()

        if (none { it.property == "id" }) {
            orderSpecifiers.add(QCourseProduct.courseProduct.id.asc())
        }

        return orderSpecifiers.toTypedArray()
    }

    override fun findVisibleCatalogProducts(
        types: Collection<ProductType>?,
        sort: ProductCatalogSort,
        pageable: Pageable,
    ): Page<Product> {
        val qProduct = QProduct.product
        val qCourseProduct = QCourseProduct.courseProduct
        val popularityCount = enrollment.id.countDistinct()
        val earliestDeadline = course.enrollmentDeadLine.min()
        val deadlineNotPassed = course.enrollmentDeadLine.isNull.or(course.enrollmentDeadLine.goe(LocalDateTime.now()))
        val where =
            listOfNotNull(
                qProduct.visible.isTrue,
                resolveTypeCondition(qProduct, types),
                resolveCatalogVisibilityCondition(qProduct, qCourseProduct, deadlineNotPassed),
            ).toTypedArray()

        val content =
            queryFactory
                .select(qProduct, popularityCount, earliestDeadline)
                .from(qProduct)
                .leftJoin(qCourseProduct)
                .on(qCourseProduct.id.eq(qProduct.id))
                .leftJoin(enrollment)
                .on(
                    enrollment.productId.eq(qProduct.id),
                    enrollment.status.`in`(
                        EnrollmentStatus.PENDING_MATCH,
                        EnrollmentStatus.ACTIVE,
                        EnrollmentStatus.COMPLETED,
                    ),
                ).leftJoin(course)
                .on(
                    course.productId.eq(qProduct.id),
                    course.status.eq(CourseStatus.LISTED),
                    deadlineNotPassed,
                ).where(*where)
                .groupBy(qProduct)
                .orderBy(*sort.toOrderSpecifiers(qProduct, popularityCount.desc(), earliestDeadline.asc().nullsLast()))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
                .map { tuple -> tuple[qProduct]!! }

        val total =
            queryFactory
                .select(qProduct.count())
                .from(qProduct)
                .leftJoin(qCourseProduct)
                .on(qCourseProduct.id.eq(qProduct.id))
                .where(*where)
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun findVisibleCatalogProductById(productId: String): Product? =
        queryFactory
            .selectFrom(QProduct.product)
            .leftJoin(QCourseProduct.courseProduct)
            .on(QCourseProduct.courseProduct.id.eq(QProduct.product.id))
            .where(
                QProduct.product.id.eq(productId),
                QProduct.product.visible.isTrue,
                resolveCatalogVisibilityCondition(
                    qProduct = QProduct.product,
                    qCourseProduct = QCourseProduct.courseProduct,
                    deadlineNotPassed = course.enrollmentDeadLine.isNull.or(course.enrollmentDeadLine.goe(LocalDateTime.now())),
                ),
            ).fetchOne()

    private fun resolveCatalogVisibilityCondition(
        qProduct: QProduct,
        qCourseProduct: QCourseProduct,
        deadlineNotPassed: BooleanExpression,
    ): BooleanExpression {
        val hasOpenListedCourse =
            queryFactory
                .selectOne()
                .from(course)
                .where(
                    course.productId.eq(qProduct.id),
                    course.status.eq(CourseStatus.LISTED),
                    deadlineNotPassed,
                ).exists()
        val courseCatalogCondition =
            qProduct
                .instanceOf(CourseProduct::class.java)
                .and(
                    qCourseProduct.requiresMatching.isTrue
                        .or(hasOpenListedCourse),
                )

        return qProduct.instanceOf(CourseProduct::class.java).not().or(courseCatalogCondition)
    }

    private fun resolveTypeCondition(
        qProduct: QProduct,
        types: Collection<ProductType>?,
    ): BooleanExpression? {
        val normalized = types?.distinct()?.takeIf { it.isNotEmpty() } ?: return null
        return normalized
            .map { qProduct.instanceOf(it.entityClass) }
            .reduce(BooleanExpression::or)
    }

    private fun ProductCatalogSort.toOrderSpecifiers(
        qProduct: QProduct,
        popularityOrder: OrderSpecifier<Long>,
        deadlineOrder: OrderSpecifier<LocalDateTime>,
    ): Array<OrderSpecifier<*>> =
        when (this) {
            ProductCatalogSort.LATEST -> arrayOf(qProduct.createdAt.desc(), qProduct.id.asc())
            ProductCatalogSort.POPULARITY -> arrayOf(popularityOrder, qProduct.createdAt.desc(), qProduct.id.asc())
            ProductCatalogSort.DEADLINE_ASC -> arrayOf(deadlineOrder, qProduct.createdAt.desc(), qProduct.id.asc())
        }
}
