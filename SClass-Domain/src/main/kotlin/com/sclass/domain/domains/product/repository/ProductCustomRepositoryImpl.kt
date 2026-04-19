package com.sclass.domain.domains.product.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.domain.QProduct
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

    override fun findVisibleByType(
        type: ProductType,
        pageable: Pageable,
    ): Page<Product> {
        val where =
            arrayOf(
                QProduct.product.visible.isTrue,
                QProduct.product.instanceOf(type.entityClass),
            )
        val content =
            queryFactory
                .selectFrom(QProduct.product)
                .where(*where)
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .orderBy(QProduct.product.createdAt.desc())
                .fetch()
        val total =
            queryFactory
                .select(QProduct.product.count())
                .from(QProduct.product)
                .where(*where)
                .fetchOne() ?: 0L
        return PageImpl(content, pageable, total)
    }

    override fun findByType(
        type: ProductType,
        pageable: Pageable,
    ): Page<Product> {
        val where = QProduct.product.instanceOf(type.entityClass)
        val content =
            queryFactory
                .selectFrom(QProduct.product)
                .where(where)
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .orderBy(QProduct.product.createdAt.desc())
                .fetch()
        val total =
            queryFactory
                .select(QProduct.product.count())
                .from(QProduct.product)
                .where(where)
                .fetchOne() ?: 0L
        return PageImpl(content, pageable, total)
    }
}
