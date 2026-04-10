package com.sclass.backoffice.product.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreateCommissionProductRequest(
    @field:NotBlank val name: String,
    @field:Min(1) val coinCost: Int,
    @field:Min(0) val teacherPayoutAmountWon: Int,
)
