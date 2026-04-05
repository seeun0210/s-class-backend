package com.sclass.domain.domains.coin.repository

import com.sclass.domain.domains.coin.domain.CoinBalance
import org.springframework.data.jpa.repository.JpaRepository

interface CoinBalanceRepository : JpaRepository<CoinBalance, String> {
    fun findByUserId(userId: String): CoinBalance?
}
