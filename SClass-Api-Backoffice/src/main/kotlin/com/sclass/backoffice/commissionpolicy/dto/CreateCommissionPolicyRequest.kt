package com.sclass.backoffice.commissionpolicy.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreateCommissionPolicyRequest(
    @field:NotBlank val name: String,
    @field:Min(1) val coinCost: Int,
)
