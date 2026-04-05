package com.sclass.infrastructure.nicepay.dto

data class PgApproveResult(
    val tid: String,
    val pgOrderId: String,
    val amount: Int,
)
