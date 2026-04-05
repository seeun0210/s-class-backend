package com.sclass.supporters.product.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.supporters.product.dto.ProductListResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetProductListUseCase(
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): ProductListResponse = ProductListResponse.from(productAdaptor.findAllActiveCoinProducts())
}
