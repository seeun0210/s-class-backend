package com.sclass.domain.domains.product.repository

import com.sclass.domain.domains.product.domain.CoinProduct

interface ProductCustomRepository {
    fun findAllActiveCoinProducts(): List<CoinProduct>
}
