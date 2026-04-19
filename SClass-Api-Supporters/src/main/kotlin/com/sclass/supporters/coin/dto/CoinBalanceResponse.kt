package com.sclass.supporters.coin.dto

import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import java.time.LocalDateTime

data class CoinBalanceResponse(
    val balance: Int,
    val lots: List<CoinLotItem>,
) {
    data class CoinLotItem(
        val lotId: String,
        val remaining: Int,
        val expireAt: LocalDateTime?,
        val sourceType: CoinLotSourceType,
    )

    companion object {
        fun of(
            balance: Int,
            activeLots: List<CoinLot>,
        ): CoinBalanceResponse =
            CoinBalanceResponse(
                balance = balance,
                lots =
                    activeLots.map {
                        CoinLotItem(
                            lotId = it.id,
                            remaining = it.remaining,
                            expireAt = it.expireAt,
                            sourceType = it.sourceType,
                        )
                    },
            )
    }
}
