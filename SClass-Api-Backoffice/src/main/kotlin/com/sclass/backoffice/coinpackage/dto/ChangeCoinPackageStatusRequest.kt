package com.sclass.backoffice.coinpackage.dto

import com.sclass.domain.domains.coin.domain.CoinPackageStatus
import jakarta.validation.constraints.NotNull

data class ChangeCoinPackageStatusRequest(
    @field:NotNull
    val status: CoinPackageStatus,
)
