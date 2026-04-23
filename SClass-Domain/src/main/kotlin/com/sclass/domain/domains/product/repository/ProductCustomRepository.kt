package com.sclass.domain.domains.product.repository

import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductCatalogSort
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.dto.MembershipProductWithCoinPackageDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductCustomRepository {
    fun findAllActiveByType(type: ProductType?): List<Product>

    fun findMembershipsWithCoinPackage(
        type: ProductType?,
        visibleOnly: Boolean,
        pageable: Pageable,
    ): Page<MembershipProductWithCoinPackageDto>

    fun findVisibleCatalogProducts(
        types: Collection<ProductType>?,
        sort: ProductCatalogSort,
        pageable: Pageable,
    ): Page<Product>

    fun findVisibleCatalogProductById(productId: String): Product?
}
