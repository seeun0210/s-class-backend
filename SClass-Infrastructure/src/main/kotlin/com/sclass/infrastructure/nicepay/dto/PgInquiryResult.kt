package com.sclass.infrastructure.nicepay.dto

data class PgInquiryResult(
    val approved: Boolean,
    val tid: String?,
    val amount: Int,
)
