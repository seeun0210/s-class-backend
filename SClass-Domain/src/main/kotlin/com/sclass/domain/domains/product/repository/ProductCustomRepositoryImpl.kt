package com.sclass.domain.domains.product.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.coin.domain.QCoinPackage
import com.sclass.domain.domains.product.domain.MembershipProduct
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
}
