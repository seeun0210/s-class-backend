package com.sclass.infrastructure.nicepay

import com.sclass.infrastructure.nicepay.dto.PgApproveResult
import com.sclass.infrastructure.nicepay.dto.PgCancelResult

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
}
