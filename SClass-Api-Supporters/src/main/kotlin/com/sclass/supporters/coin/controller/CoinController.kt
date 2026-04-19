package com.sclass.supporters.coin.controller

import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import com.sclass.supporters.coin.dto.CoinBalanceResponse
import com.sclass.supporters.coin.dto.CoinTransactionHistoryResponse
import com.sclass.supporters.coin.usecase.GetCoinBalanceUseCase
import com.sclass.supporters.coin.usecase.GetCoinTransactionHistoryUseCase
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/coins")
class CoinController(
    private val getCoinBalanceUseCase: GetCoinBalanceUseCase,
    private val getCoinTransactionHistoryUseCase: GetCoinTransactionHistoryUseCase,
) {
    @GetMapping("/balance")
    fun getBalance(
        @CurrentUserId userId: String,
    ): ApiResponse<CoinBalanceResponse> = ApiResponse.success(getCoinBalanceUseCase.execute(userId))

    @GetMapping("/transactions")
    fun getTransactions(
        @CurrentUserId userId: String,
        @RequestParam(required = false) type: CoinTransactionType?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<CoinTransactionHistoryResponse> =
        ApiResponse.success(getCoinTransactionHistoryUseCase.execute(userId, type, from, to, page, size))
}
