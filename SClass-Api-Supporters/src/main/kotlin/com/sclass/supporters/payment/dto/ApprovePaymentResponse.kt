package com.sclass.supporters.payment.dto

import com.sclass.domain.domains.payment.domain.PaymentStatus

data class ApprovePaymentResponse(
    val paymentId: String,
    val status: PaymentStatus,
    val issuedCoinAmount: Int,
)
