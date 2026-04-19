package com.sclass.backoffice.coin.usecase

import com.sclass.backoffice.coin.dto.AdminCoinTransactionHistoryResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@UseCase
class GetAdminCoinTransactionHistoryUseCase(
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
    ): AdminCoinTransactionHistoryResponse {
        val pageable = PageRequest.of(page, size)
        val result = coinAdaptor.findGroupedTransactions(userId, type, from, to, pageable)
        return AdminCoinTransactionHistoryResponse.from(result, page)
    }
}
