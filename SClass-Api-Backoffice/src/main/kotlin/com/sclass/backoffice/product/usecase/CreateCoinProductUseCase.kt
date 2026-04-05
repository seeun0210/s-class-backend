package com.sclass.backoffice.product.usecase

import com.sclass.backoffice.product.dto.CreateCoinProductRequest
import com.sclass.backoffice.product.dto.ProductResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.domain.domains.product.domain.CoinProduct
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCoinProductUseCase(
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(request: CreateCoinProductRequest): ProductResponse {
        val product =
            productAdaptor.save(
                CoinProduct(
                    name = request.name,
                    priceWon = request.priceWon,
                    coinAmount =
                        request.coinAmount,
                ),
            )
        return ProductResponse.from(product)
    }
}
