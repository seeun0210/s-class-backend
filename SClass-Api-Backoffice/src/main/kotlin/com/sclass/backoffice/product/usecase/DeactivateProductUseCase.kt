package com.sclass.backoffice.product.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeactivateProductUseCase(
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    fun execute(id: String) {
        val product = productAdaptor.findById(id)
        product.active = false
    }
}
