package com.sclass.domain.domains.product.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import com.sclass.domain.domains.product.domain.CommissionProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.exception.CommissionProductNotConfiguredException
import com.sclass.domain.domains.product.exception.ProductNotFoundException
import com.sclass.domain.domains.product.repository.ProductRepository

@Adaptor
class ProductAdaptor(
    private val productRepository: ProductRepository,
) {
    fun findById(id: String): Product = productRepository.findById(id).orElseThrow { ProductNotFoundException() }

    fun findAllActive(): List<Product> = productRepository.findAllByActiveTrue()

    fun save(product: Product): Product = productRepository.save(product)

    fun findAllActiveCoinProducts(): List<CoinProduct> = productRepository.findAllActiveCoinProducts()

    fun findActiveCommissionProduct(): CommissionProduct =
        productRepository.findActiveCommissionProduct()
            ?: throw CommissionProductNotConfiguredException()
}
