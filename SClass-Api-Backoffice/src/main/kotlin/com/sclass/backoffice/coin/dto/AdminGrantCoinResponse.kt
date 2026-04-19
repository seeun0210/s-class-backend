package com.sclass.backoffice.coin.dto

import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import java.time.LocalDateTime

data class AdminGrantCoinResponse(
    val lotId: String,
    val userId: String,
    val amount: Int,
    val sourceType: CoinLotSourceType,
    val expireAt: LocalDateTime?,
) {
    companion object {
        fun from(lot: CoinLot) =
            AdminGrantCoinResponse(
                lotId = lot.id,
                userId = lot.userId,
                amount = lot.amount,
                sourceType = lot.sourceType,
                expireAt = lot.expireAt,
            )
    }
}
