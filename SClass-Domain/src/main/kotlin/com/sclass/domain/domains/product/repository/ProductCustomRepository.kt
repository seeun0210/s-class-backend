package com.sclass.domain.domains.product.repository

import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.CommissionProduct

interface ProductCustomRepository {
    fun findAllActiveCoinProducts(): List<CoinProduct>

    fun findActiveCommissionProduct(): CommissionProduct?
}
