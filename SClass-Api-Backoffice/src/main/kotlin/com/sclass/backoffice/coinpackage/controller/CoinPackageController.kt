package com.sclass.backoffice.coinpackage.controller

import com.sclass.backoffice.coinpackage.dto.CoinPackageListResponse
import com.sclass.backoffice.coinpackage.dto.CoinPackageResponse
import com.sclass.backoffice.coinpackage.dto.CreateCoinPackageRequest
import com.sclass.backoffice.coinpackage.usecase.CreateCoinPackageUseCase
import com.sclass.backoffice.coinpackage.usecase.DeactivateCoinPackageUseCase
import com.sclass.backoffice.coinpackage.usecase.GetAdminCoinPackageListUseCase
import com.sclass.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/coin-packages")
class CoinPackageController(
    private val createCoinPackageUseCase: CreateCoinPackageUseCase,
    private val getAdminCoinPackageListUseCase: GetAdminCoinPackageListUseCase,
    private val deactivateCoinPackageUseCase: DeactivateCoinPackageUseCase,
) {
    @GetMapping
    fun getCoinPackages(): ApiResponse<CoinPackageListResponse> = ApiResponse.success(getAdminCoinPackageListUseCase.execute())

    @PostMapping
    fun createCoinPackage(
        @RequestBody @Valid request: CreateCoinPackageRequest,
    ): ApiResponse<CoinPackageResponse> = ApiResponse.success(createCoinPackageUseCase.execute(request))

    @PatchMapping("/{id}/deactivate")
    fun deactivate(
        @PathVariable id: String,
    ): ApiResponse<Unit> {
        deactivateCoinPackageUseCase.execute(id)
        return ApiResponse.success(Unit)
    }
}
