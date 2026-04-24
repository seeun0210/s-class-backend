package com.sclass.backoffice.product.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.product.adaptor.ProductAdaptor
import com.sclass.infrastructure.redis.DistributedLock
import com.sclass.infrastructure.redis.LockKey
import org.springframework.transaction.annotation.Transactional

@UseCase
class SetProductVisibilityUseCase(
    private val productAdaptor: ProductAdaptor,
) {
    @Transactional
    @DistributedLock(prefix = "course-product")
    fun execute(
        @LockKey id: String,
        visible: Boolean,
    ) {
        val product = productAdaptor.findById(id)
        if (visible) product.show() else product.hide()
    }
}
