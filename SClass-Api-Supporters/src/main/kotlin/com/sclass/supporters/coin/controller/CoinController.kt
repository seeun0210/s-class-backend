package com.sclass.supporters.coin.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.coin.dto.CoinBalanceResponse
import com.sclass.supporters.coin.usecase.GetCoinBalanceUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/coins")
class CoinController(
    private val getCoinBalanceUseCase: GetCoinBalanceUseCase,
) {
    @GetMapping("/balance")
    fun getBalance(
        @CurrentUserId userId: String,
    ): ApiResponse<CoinBalanceResponse> = ApiResponse.success(getCoinBalanceUseCase.execute(userId))
}
