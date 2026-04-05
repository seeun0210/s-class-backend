package com.sclass.backoffice.product.dto

import com.sclass.domain.domains.product.domain.Product

data class ProductListResponse(
    val products: List<ProductResponse>,
) {
    companion object {
        fun from(products: List<Product>) = ProductListResponse(products.map { ProductResponse.from(it) })
    }
}
