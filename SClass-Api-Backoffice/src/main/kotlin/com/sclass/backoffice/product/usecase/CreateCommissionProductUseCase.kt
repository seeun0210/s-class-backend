package com.sclass.backoffice.product.usecase

import com.sclass.backoffice.product.dto.CreateCommissionProductRequest
import com.sclass.backoffice.product.dto.ProductResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CommissionProduct
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCommissionProductUseCase(
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(request: CreateCommissionProductRequest): ProductResponse {
        val product =
            productAdaptor.save(
                CommissionProduct(
                    name = request.name,
                    coinCost = request.coinCost,
                ),
            )
        return ProductResponse.from(product)
    }
}
