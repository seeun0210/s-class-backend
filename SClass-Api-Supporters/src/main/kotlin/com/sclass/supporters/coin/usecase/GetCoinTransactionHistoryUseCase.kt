package com.sclass.supporters.coin.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import com.sclass.supporters.coin.dto.CoinTransactionHistoryResponse
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@UseCase
class GetCoinTransactionHistoryUseCase(
    private val coinAdaptor: CoinAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        type: CoinTransactionType?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        page: Int,
        size: Int,
    ): CoinTransactionHistoryResponse {
        val pageable = PageRequest.of(page, size)
        val result = coinAdaptor.findGroupedTransactions(userId, type, from, to, pageable)
        return CoinTransactionHistoryResponse.from(result, page)
    }
}
