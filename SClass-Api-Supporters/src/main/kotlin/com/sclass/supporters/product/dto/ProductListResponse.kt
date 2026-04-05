package com.sclass.supporters.product.dto

import com.sclass.domain.domains.product.domain.CoinProduct

data class ProductListResponse(
    val products: List<ProductItem>,
) {
    data class ProductItem(
        val productId: String,
        val name: String,
        val priceWon: Int,
        val coinAmount: Int,
    )

    companion object {
        fun from(products: List<CoinProduct>) =
            ProductListResponse(
                products =
                    products.map {
                        ProductItem(
                            productId = it.id,
                            name = it.name,
                            priceWon = it.priceWon,
                            coinAmount = it.coinAmount,
                        )
                    },
            )
    }
}
