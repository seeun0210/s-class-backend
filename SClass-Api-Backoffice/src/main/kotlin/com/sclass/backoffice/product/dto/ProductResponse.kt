package com.sclass.backoffice.product.dto

import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.CommissionProduct
import com.sclass.domain.domains.product.domain.Product

data class ProductResponse(
    val id: String,
    val name: String,
    val type: String,
    val priceWon: Int,
    val coinAmount: Int?,
    val coinCost: Int?,
    val active: Boolean,
) {
    companion object {
        fun from(product: Product) =
            ProductResponse(
                id = product.id,
                name = product.name,
                type =
                    when (product) {
                        is CoinProduct -> "COIN"
                        is CommissionProduct -> "COMMISSION"
                        else -> "UNKNOWN"
                    },
                priceWon = product.priceWon,
                coinAmount = (product as? CoinProduct)?.coinAmount,
                coinCost = (product as? CommissionProduct)?.coinCost,
                active = product.active,
            )
    }
}
