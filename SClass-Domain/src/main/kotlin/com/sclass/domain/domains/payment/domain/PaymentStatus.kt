package com.sclass.domain.domains.payment.domain

enum class PaymentStatus {
    PENDING,
    PG_APPROVED,
    COMPLETED,
    PG_APPROVE_FAILED,
    ISSUE_COIN_FAILED,
    COMPENSATION_NEEDED,
    CANCELLED,
    PG_CANCEL_FAILED,
    COIN_REFUND_FAILED,
}
