package com.sclass.domain.domains.product.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.coin.domain.QCoinPackage
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.QMembershipProduct
import com.sclass.domain.domains.product.domain.QProduct
import com.sclass.domain.domains.product.dto.MembershipProductWithCoinPackageDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

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
        visibleOnly: Boolean,
        pageable: Pageable,
    ): Page<MembershipProductWithCoinPackageDto> {
        val qMembership = QMembershipProduct.membershipProduct
        val qCoin = QCoinPackage.coinPackage
        val where: BooleanExpression? = if (visibleOnly) qMembership.visible.isTrue else null

        val content =
            queryFactory
                .select(qMembership, qCoin)
                .from(qMembership)
                .leftJoin(qCoin)
                .on(qMembership.coinPackageId.eq(qCoin.id))
                .where(where)
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
                .where(where)
                .fetchOne() ?: 0L
        return PageImpl(content, pageable, total)
    }
}
