package com.sclass.domain.domains.payment.domain

enum class PaymentCancelSource {
    USER_ABANDONED,
    PAYMENT_TIMEOUT,
    ;

    fun pendingMetadata(): String = name

    fun compensatedMetadata(): String = "${name}_PG_CANCELLED"

    fun compensationReason(): String =
        when (this) {
            USER_ABANDONED -> "사용자 결제 포기 자동 승인취소"
            PAYMENT_TIMEOUT -> "결제 시간 초과 자동 승인취소"
        }

    companion object {
        fun fromPendingMetadata(metadata: String?): PaymentCancelSource? = entries.firstOrNull { it.pendingMetadata() == metadata }
    }
}
