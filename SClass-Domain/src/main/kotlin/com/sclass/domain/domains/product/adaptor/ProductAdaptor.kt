package com.sclass.domain.domains.product.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.product.domain.MembershipProduct
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.exception.ProductNotFoundException
import com.sclass.domain.domains.product.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class ProductAdaptor(
    private val productRepository: ProductRepository,
) {
    fun findById(id: String): Product = productRepository.findById(id).orElseThrow { ProductNotFoundException() }

    fun findAllActive(type: ProductType? = null): List<Product> = productRepository.findAllActiveByType(type)

    fun save(product: Product): Product = productRepository.save(product)

    @Suppress("UNCHECKED_CAST")
    fun findVisibleMemberships(pageable: Pageable): Page<MembershipProduct> =
        productRepository.findVisibleByType(ProductType.MEMBERSHIP, pageable) as Page<MembershipProduct>

    @Suppress("UNCHECKED_CAST")
    fun findAllMemberships(pageable: Pageable): Page<MembershipProduct> =
        productRepository.findByType(ProductType.MEMBERSHIP, pageable) as Page<MembershipProduct>
}
