package com.sclass.backoffice.coinpackage.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class DeactivateCoinPackageUseCase(
    private val coinPackageAdaptor: CoinPackageAdaptor,
) {
    @Transactional
    fun execute(id: String) {
        val coinPackage = coinPackageAdaptor.findById(id)
        coinPackage.active = false
    }
}
