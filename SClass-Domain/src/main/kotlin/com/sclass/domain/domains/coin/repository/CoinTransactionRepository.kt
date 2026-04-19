package com.sclass.domain.domains.coin.repository

import com.sclass.domain.domains.coin.domain.CoinTransaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CoinTransactionRepository :
    JpaRepository<CoinTransaction, String>,
    CoinTransactionCustomRepository {
    fun findAllByUserId(
        userId: String,
        pageable: Pageable,
    ): Page<CoinTransaction>
}
