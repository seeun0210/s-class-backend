package com.sclass.domain.domains.product.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.QCoinProduct

class ProductCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : ProductCustomRepository {
    override fun findAllActiveCoinProducts(): List<CoinProduct> =
        queryFactory
            .select(QCoinProduct.coinProduct)
            .from(QCoinProduct.coinProduct)
            .where(QCoinProduct.coinProduct.active.isTrue)
            .fetch()
}
