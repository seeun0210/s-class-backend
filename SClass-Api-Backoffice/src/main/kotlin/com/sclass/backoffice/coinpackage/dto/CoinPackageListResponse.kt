package com.sclass.backoffice.coinpackage.dto

import com.sclass.domain.domains.coin.domain.CoinPackage

data class CoinPackageListResponse(
    val coinPackages: List<CoinPackageResponse>,
) {
    companion object {
        fun from(coinPackages: List<CoinPackage>) = CoinPackageListResponse(coinPackages.map { CoinPackageResponse.from(it) })
    }
}
