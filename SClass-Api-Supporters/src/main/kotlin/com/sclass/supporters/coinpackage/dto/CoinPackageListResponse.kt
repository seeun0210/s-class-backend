package com.sclass.supporters.coinpackage.dto

import com.sclass.domain.domains.coin.domain.CoinPackage

data class CoinPackageListResponse(
    val coinPackages: List<CoinPackageItem>,
) {
    data class CoinPackageItem(
        val coinPackageId: String,
        val name: String,
        val priceWon: Int,
        val coinAmount: Int,
    )

    companion object {
        fun from(coinPackages: List<CoinPackage>) =
            CoinPackageListResponse(
                coinPackages =
                    coinPackages.map {
                        CoinPackageItem(
                            coinPackageId = it.id,
                            name = it.name,
                            priceWon = it.priceWon,
                            coinAmount = it.coinAmount,
                        )
                    },
            )
    }
}
