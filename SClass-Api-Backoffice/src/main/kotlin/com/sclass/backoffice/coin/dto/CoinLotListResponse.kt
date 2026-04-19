package com.sclass.backoffice.coin.dto

import com.sclass.domain.domains.coin.domain.CoinLot
import com.sclass.domain.domains.coin.domain.CoinLotSourceType
import org.springframework.data.domain.Page
import java.time.LocalDateTime

data class CoinLotListResponse(
    val content: List<CoinLotItem>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
) {
    data class CoinLotItem(
        val lotId: String,
        val amount: Int,
        val remaining: Int,
        val enrollmentId: Long?,
        val sourceType: CoinLotSourceType,
        val expireAt: LocalDateTime?,
        val createdAt: LocalDateTime,
    )

    companion object {
        fun from(
            page: Page<CoinLot>,
            currentPage: Int,
        ) = CoinLotListResponse(
            content =
                page.content.map {
                    CoinLotItem(
                        lotId = it.id,
                        amount = it.amount,
                        remaining = it.remaining,
                        enrollmentId = it.enrollmentId,
                        sourceType = it.sourceType,
                        expireAt = it.expireAt,
                        createdAt = it.createdAt,
                    )
                },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = currentPage,
        )
    }
}
