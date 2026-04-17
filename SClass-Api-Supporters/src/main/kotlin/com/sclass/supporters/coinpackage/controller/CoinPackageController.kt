package com.sclass.supporters.coinpackage.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.coinpackage.dto.CoinPackageListResponse
import com.sclass.supporters.coinpackage.usecase.GetCoinPackageListUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/coin-packages")
class CoinPackageController(
    private val getCoinPackageListUseCase: GetCoinPackageListUseCase,
) {
    @GetMapping
    fun getCoinPackages(): ApiResponse<CoinPackageListResponse> = ApiResponse.success(getCoinPackageListUseCase.execute())
}
