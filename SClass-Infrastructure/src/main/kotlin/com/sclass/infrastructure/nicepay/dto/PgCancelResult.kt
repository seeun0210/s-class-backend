package com.sclass.infrastructure.nicepay.dto

data class PgCancelResult(
    val tid: String,
    val cancelAmount: Int,
)
