package com.sclass.domain.domains.product.repository

import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType

interface ProductCustomRepository {
    fun findAllActiveByType(type: ProductType?): List<Product>
}
