package com.sclass.domain.domains.coin.repository

import com.sclass.domain.domains.coin.domain.CoinLot
import java.time.LocalDateTime

interface CoinLotCustomRepository {
    fun findActiveForUpdate(
        userId: String,
        now: LocalDateTime,
    ): List<CoinLot>

    fun sumActive(
        userId: String,
        now: LocalDateTime,
    ): Int

    fun findActive(
        userId: String,
        now: LocalDateTime,
    ): List<CoinLot>

    fun findExpiringBefore(
        now: LocalDateTime,
        limit: Int,
    ): List<CoinLot>
}
