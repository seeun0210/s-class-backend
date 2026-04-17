package com.sclass.backoffice.coinpackage.usecase

import com.sclass.backoffice.coinpackage.dto.CoinPackageListResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetAdminCoinPackageListUseCase(
    private val coinPackageAdaptor: CoinPackageAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): CoinPackageListResponse = CoinPackageListResponse.from(coinPackageAdaptor.findAllActive())
}
