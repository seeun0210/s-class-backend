package com.sclass.backoffice.product.usecase

import com.sclass.backoffice.product.dto.ProductListResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.ProductType

@UseCase
class GetAdminProductListUseCase(
    private val productAdaptor: ProductAdaptor,
) {
    fun execute(type: ProductType?): ProductListResponse = ProductListResponse.from(productAdaptor.findAllActive(type))
}
