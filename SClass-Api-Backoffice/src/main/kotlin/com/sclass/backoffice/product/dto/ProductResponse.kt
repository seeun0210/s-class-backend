package com.sclass.backoffice.product.dto

import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.CommissionProduct
import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.exception.UnknownProductTypeException

data class ProductResponse(
    val id: String,
    val name: String,
    val type: ProductType,
    val priceWon: Int,
    val coinAmount: Int?,
    val coinCost: Int?,
    val totalLessons: Int?,
    val active: Boolean,
) {
    companion object {
        fun from(product: Product) =
            ProductResponse(
                id = product.id,
                name = product.name,
                type =
                    when (product) {
                        is CoinProduct -> ProductType.COIN
                        is CommissionProduct -> ProductType.COMMISSION
                        is CourseProduct -> ProductType.COURSE
                        else -> throw UnknownProductTypeException()
                    },
                priceWon = product.priceWon,
                coinAmount = (product as? CoinProduct)?.coinAmount,
                coinCost = (product as? CommissionProduct)?.coinCost,
                totalLessons = (product as? CourseProduct)?.totalLessons,
                active = product.active,
            )
    }
}
