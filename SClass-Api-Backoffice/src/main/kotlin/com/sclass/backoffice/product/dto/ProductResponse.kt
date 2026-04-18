package com.sclass.backoffice.product.dto

import com.sclass.domain.domains.product.domain.CourseProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.exception.UnknownProductTypeException

data class ProductResponse(
    val id: String,
    val name: String,
    val priceWon: Int,
    val totalLessons: Int?,
    val visible: Boolean,
) {
    companion object {
        fun from(product: Product) =
            when (product) {
                is CourseProduct ->
                    ProductResponse(
                        id = product.id,
                        name = product.name,
                        priceWon = product.priceWon,
                        totalLessons = product.totalLessons,
                        visible = product.visible,
                    )
                else -> throw UnknownProductTypeException()
            }
    }
}
