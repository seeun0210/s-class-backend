package com.sclass.supporters.membership.dto

data class PrepareMembershipPurchaseResponse(
    val paymentId: String,
    val pgOrderId: String,
    val amount: Int,
    val productId: String,
    val productName: String,
)
