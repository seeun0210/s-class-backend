package com.sclass.backoffice.commissionpolicy.dto

import jakarta.validation.constraints.NotNull

data class UpdateCommissionPolicyActiveRequest(
    @field:NotNull val active: Boolean,
)
