package com.sclass.domain.domains.product.repository

import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.CommissionProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType

interface ProductCustomRepository {
    fun findAllActiveCoinProducts(): List<CoinProduct>

    fun findActiveCommissionProduct(): CommissionProduct?

    fun findAllActiveByType(type: ProductType?): List<Product>
}
