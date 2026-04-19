package com.sclass.supporters.membership.dto

import com.sclass.domain.domains.payment.domain.PgType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PrepareMembershipPurchaseRequest(
    @field:NotBlank val membershipProductId: String,
    @field:NotNull val pgType: PgType,
)
