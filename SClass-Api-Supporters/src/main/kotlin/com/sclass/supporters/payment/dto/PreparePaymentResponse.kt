package com.sclass.supporters.payment.dto

data class PreparePaymentResponse(
    val paymentId: String,
    val pgOrderId: String,
    val amount: Int,
    val productName: String,
)
