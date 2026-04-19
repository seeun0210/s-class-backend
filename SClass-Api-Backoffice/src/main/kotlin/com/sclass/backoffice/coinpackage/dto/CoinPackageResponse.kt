package com.sclass.backoffice.coinpackage.dto

import com.sclass.domain.domains.coin.domain.CoinPackage
import com.sclass.domain.domains.coin.domain.CoinPackageStatus

data class CoinPackageResponse(
    val id: String,
    val name: String,
    val priceWon: Int,
    val coinAmount: Int,
    val status: CoinPackageStatus,
) {
    companion object {
        fun from(coinPackage: CoinPackage) =
            CoinPackageResponse(
                id = coinPackage.id,
                name = coinPackage.name,
                priceWon = coinPackage.priceWon,
                coinAmount = coinPackage.coinAmount,
                status = coinPackage.status,
            )
    }
}
