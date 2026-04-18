package com.sclass.domain.domains.product.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.QProduct

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
}
