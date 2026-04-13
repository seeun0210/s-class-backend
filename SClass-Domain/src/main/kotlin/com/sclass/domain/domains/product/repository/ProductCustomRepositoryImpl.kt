package com.sclass.domain.domains.product.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.CommissionProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.QCoinProduct
import com.sclass.domain.domains.product.domain.QCommissionProduct
import com.sclass.domain.domains.product.domain.QProduct

class ProductCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : ProductCustomRepository {
    override fun findAllActiveCoinProducts(): List<CoinProduct> =
        queryFactory
            .select(QCoinProduct.coinProduct)
            .from(QCoinProduct.coinProduct)
            .where(QCoinProduct.coinProduct.active.isTrue)
            .fetch()

    override fun findActiveCommissionProduct(): CommissionProduct? =
        queryFactory
            .select(QCommissionProduct.commissionProduct)
            .from(QCommissionProduct.commissionProduct)
            .where(QCommissionProduct.commissionProduct.active.isTrue)
            .orderBy(QCommissionProduct.commissionProduct.createdAt.desc())
            .fetchFirst()

    override fun findAllActiveByType(type: ProductType?): List<Product> =
        queryFactory
            .selectFrom(QProduct.product)
            .where(
                QProduct.product.active.isTrue,
                type?.let { QProduct.product.instanceOf(it.entityClass) },
            ).fetch()
}
