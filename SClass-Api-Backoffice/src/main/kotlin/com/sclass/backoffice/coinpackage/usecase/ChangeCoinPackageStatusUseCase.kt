package com.sclass.backoffice.coinpackage.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackageStatus
import org.springframework.transaction.annotation.Transactional

@UseCase
class ChangeCoinPackageStatusUseCase(
    private val coinPackageAdaptor: CoinPackageAdaptor,
) {
    @Transactional
    fun execute(
        id: String,
        targetStatus: CoinPackageStatus,
    ) {
        val coinPackage = coinPackageAdaptor.findById(id)
        when (targetStatus) {
            CoinPackageStatus.ACTIVE -> coinPackage.activate()
            CoinPackageStatus.INACTIVE -> coinPackage.deactivate()
            CoinPackageStatus.ARCHIVED -> coinPackage.archive()
        }
    }
}
