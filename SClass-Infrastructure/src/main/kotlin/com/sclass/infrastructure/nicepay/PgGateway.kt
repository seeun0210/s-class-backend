package com.sclass.infrastructure.nicepay

import com.sclass.infrastructure.nicepay.dto.PgApproveResult
import com.sclass.infrastructure.nicepay.dto.PgCancelResult
import com.sclass.infrastructure.nicepay.dto.PgInquiryResult

interface PgGateway {
    fun approve(
        pgOrderId: String,
        tid: String,
        amount: Int,
    ): PgApproveResult

    fun cancel(
        tid: String,
        amount: Int,
        reason: String,
    ): PgCancelResult

    fun verifyWebhookSignature(
        tid: String,
        amount: Int,
        ediDate: String,
        signature: String,
    ): Boolean

    fun inquiry(pgOrderId: String): PgInquiryResult
}
