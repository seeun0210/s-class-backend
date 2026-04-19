package com.sclass.domain.domains.product.repository

import com.sclass.domain.domains.product.domain.Product
import com.sclass.domain.domains.product.domain.ProductType
import com.sclass.domain.domains.product.dto.MembershipProductWithCoinPackageDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductCustomRepository {
    fun findAllActiveByType(type: ProductType?): List<Product>

    fun findMembershipsWithCoinPackage(
        visibleOnly: Boolean,
        pageable: Pageable,
    ): Page<MembershipProductWithCoinPackageDto>
}
