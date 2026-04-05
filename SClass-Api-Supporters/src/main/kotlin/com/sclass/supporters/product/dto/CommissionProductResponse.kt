package com.sclass.supporters.product.dto

import com.sclass.domain.domains.product.domain.CommissionProduct

data class CommissionProductResponse(
    val productId: String,
    val name: String,
    val coinCost: Int,
) {
    companion object {
        fun from(product: CommissionProduct) =
            CommissionProductResponse(
                productId = product.id,
                name = product.name,
                coinCost = product.coinCost,
            )
    }
}
