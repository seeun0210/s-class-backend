package com.sclass.domain.domains.product.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductCatalogSort
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.dto.MembershipProductWithCoinPackageDto
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

    fun findMembershipsWithCoinPackage(
        type: ProductType? = null,
        visibleOnly: Boolean,
        pageable: Pageable,
    ): Page<MembershipProductWithCoinPackageDto> = productRepository.findMembershipsWithCoinPackage(type, visibleOnly, pageable)

    fun findVisibleCatalogProducts(
        types: Collection<ProductType>?,
        sort: ProductCatalogSort,
        pageable: Pageable,
    ): Page<Product> = productRepository.findVisibleCatalogProducts(types, sort, pageable)

    fun findVisibleCatalogProductById(productId: String): Product? = productRepository.findVisibleCatalogProductById(productId)
}
