package com.sclass.supporters.product.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.supporters.product.dto.CommissionProductResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCommissionProductUseCase(
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): CommissionProductResponse = CommissionProductResponse.from(productAdaptor.findActiveCommissionProduct())
}
