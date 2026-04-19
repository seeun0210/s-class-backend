package com.sclass.supporters.coin.dto

import com.sclass.domain.domains.coin.domain.CoinTransactionType
import com.sclass.domain.domains.coin.dto.CoinTransactionGroupDto
import org.springframework.data.domain.Page
import java.time.LocalDateTime

data class CoinTransactionHistoryResponse(
    val content: List<CoinTransactionItem>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
) {
    data class CoinTransactionItem(
        val type: CoinTransactionType,
        val totalAmount: Int,
        val referenceId: String?,
        val description: String?,
        val createdAt: LocalDateTime,
    )

    companion object {
        fun from(
            page: Page<CoinTransactionGroupDto>,
            currentPage: Int,
        ): CoinTransactionHistoryResponse =
            CoinTransactionHistoryResponse(
                content =
                    page.content.map {
                        CoinTransactionItem(
                            type = it.type,
                            totalAmount = it.totalAmount,
                            referenceId = it.referenceId,
                            description = it.description,
                            createdAt = it.createdAt,
                        )
                    },
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = currentPage,
            )
    }
}
