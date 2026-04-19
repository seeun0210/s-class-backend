package com.sclass.supporters.coin.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import com.sclass.supporters.coin.dto.CoinBalanceResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCoinBalanceUseCase(
    private val coinAdaptor: CoinAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(userId: String): CoinBalanceResponse = CoinBalanceResponse(balance = coinAdaptor.sumActiveLots(userId))
}
