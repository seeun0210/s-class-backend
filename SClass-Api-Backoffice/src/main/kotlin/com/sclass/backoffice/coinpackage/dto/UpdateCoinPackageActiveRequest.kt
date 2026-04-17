package com.sclass.backoffice.coinpackage.dto

import jakarta.validation.constraints.NotNull

data class UpdateCoinPackageActiveRequest(
    @field:NotNull val active: Boolean,
)
