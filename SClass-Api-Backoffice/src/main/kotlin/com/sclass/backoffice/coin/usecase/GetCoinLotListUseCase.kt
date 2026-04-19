package com.sclass.backoffice.coin.usecase

import com.sclass.backoffice.coin.dto.CoinLotListResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.coin.adaptor.CoinAdaptor
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetCoinLotListUseCase(
    private val coinAdaptor: CoinAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        page: Int,
        size: Int,
    ): CoinLotListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = coinAdaptor.findLotsByUserId(userId, pageable)
        return CoinLotListResponse.from(result, page)
    }
}
