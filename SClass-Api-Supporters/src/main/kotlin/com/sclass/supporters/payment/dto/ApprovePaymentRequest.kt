package com.sclass.supporters.payment.dto

import jakarta.validation.constraints.NotBlank

data class ApprovePaymentRequest(
    @field:NotBlank val tid: String,
)
