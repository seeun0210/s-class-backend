package com.sclass.backoffice.coin.controller

import com.sclass.backoffice.coin.dto.AdminCoinTransactionHistoryResponse
import com.sclass.backoffice.coin.dto.AdminGrantCoinRequest
import com.sclass.backoffice.coin.dto.AdminGrantCoinResponse
import com.sclass.backoffice.coin.usecase.AdminGrantCoinUseCase
import com.sclass.backoffice.coin.usecase.GetAdminCoinTransactionHistoryUseCase
import com.sclass.common.annotation.CurrentUserId
import com.sclass.common.dto.ApiResponse
import com.sclass.domain.domains.coin.domain.CoinTransactionType
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/coins")
class AdminCoinController(
    private val getAdminCoinTransactionHistoryUseCase: GetAdminCoinTransactionHistoryUseCase,
    private val adminGrantCoinUseCase: AdminGrantCoinUseCase,
) {
    @PostMapping
    fun grantCoin(
        @CurrentUserId userId: String,
        @RequestBody @Valid request: AdminGrantCoinRequest,
    ): ApiResponse<AdminGrantCoinResponse> = ApiResponse.success(adminGrantCoinUseCase.execute(userId, request))

    @GetMapping("/transactions")
    fun getTransactions(
        @RequestParam userId: String,
        @RequestParam(required = false) type: CoinTransactionType?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<AdminCoinTransactionHistoryResponse> =
        ApiResponse.success(getAdminCoinTransactionHistoryUseCase.execute(userId, type, from, to, page, size))
}
