package com.sclass.domain.domains.coin.dto

import com.sclass.domain.domains.coin.domain.CoinTransactionType
import java.time.LocalDateTime

data class CoinTransactionGroupDto(
    val type: CoinTransactionType,
    val totalAmount: Int,
    val referenceId: String?,
    val description: String?,
    val createdAt: LocalDateTime,
)
