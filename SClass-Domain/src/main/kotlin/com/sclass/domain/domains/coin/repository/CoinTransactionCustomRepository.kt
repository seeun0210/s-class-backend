package com.sclass.domain.domains.coin.repository

import com.sclass.domain.domains.coin.domain.CoinTransactionType
import com.sclass.domain.domains.coin.dto.CoinTransactionGroupDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface CoinTransactionCustomRepository {
    fun findGroupedByUser(
        userId: String,
        type: CoinTransactionType?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        pageable: Pageable,
    ): Page<CoinTransactionGroupDto>
}
