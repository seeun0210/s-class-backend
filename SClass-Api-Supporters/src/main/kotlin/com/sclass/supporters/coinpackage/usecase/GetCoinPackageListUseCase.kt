package com.sclass.supporters.coinpackage.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.supporters.coinpackage.dto.CoinPackageListResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCoinPackageListUseCase(
    private val coinPackageAdaptor: CoinPackageAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(): CoinPackageListResponse = CoinPackageListResponse.from(coinPackageAdaptor.findAllActive())
}
