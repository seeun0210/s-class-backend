package com.sclass.domain.domains.product.repository

import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductCustomRepository {
    fun findAllActiveByType(type: ProductType?): List<Product>

    fun findVisibleByType(
        type: ProductType,
        pageable: Pageable,
    ): Page<Product>

    fun findByType(
        type: ProductType,
        pageable: Pageable,
    ): Page<Product>
}
