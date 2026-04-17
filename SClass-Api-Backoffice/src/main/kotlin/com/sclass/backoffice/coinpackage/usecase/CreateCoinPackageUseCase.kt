package com.sclass.backoffice.coinpackage.usecase

import com.sclass.backoffice.coinpackage.dto.CoinPackageResponse
import com.sclass.backoffice.coinpackage.dto.CreateCoinPackageRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinPackageAdaptor
import com.sclass.domain.domains.coin.domain.CoinPackage
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCoinPackageUseCase(
    private val coinPackageAdaptor: CoinPackageAdaptor,
) {
    @Transactional
    fun execute(request: CreateCoinPackageRequest): CoinPackageResponse {
        val coinPackage =
            coinPackageAdaptor.save(
                CoinPackage(
                    name = request.name,
                    priceWon = request.priceWon,
                    coinAmount = request.coinAmount,
                ),
            )
        return CoinPackageResponse.from(coinPackage)
    }
}
