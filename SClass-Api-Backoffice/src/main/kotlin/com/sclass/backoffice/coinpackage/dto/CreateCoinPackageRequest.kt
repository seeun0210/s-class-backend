package com.sclass.backoffice.coinpackage.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreateCoinPackageRequest(
    @field:NotBlank val name: String,
    @field:Min(0) val priceWon: Int,
    @field:Min(1) val coinAmount: Int,
)
