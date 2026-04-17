package com.sclass.backoffice.coinpackage.dto

import com.sclass.domain.domains.coin.domain.CoinPackage

data class CoinPackageResponse(
    val id: String,
    val name: String,
    val priceWon: Int,
    val coinAmount: Int,
    val active: Boolean,
) {
    companion object {
        fun from(coinPackage: CoinPackage) =
            CoinPackageResponse(
                id = coinPackage.id,
                name = coinPackage.name,
                priceWon = coinPackage.priceWon,
                coinAmount = coinPackage.coinAmount,
                active = coinPackage.active,
            )
    }
}
