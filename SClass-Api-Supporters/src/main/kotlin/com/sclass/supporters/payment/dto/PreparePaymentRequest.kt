package com.sclass.supporters.payment.dto

import com.sclass.domain.domains.payment.domain.PgType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PreparePaymentRequest(
    @field:NotBlank val productId: String,
    @field:NotNull val pgType: PgType,
)
