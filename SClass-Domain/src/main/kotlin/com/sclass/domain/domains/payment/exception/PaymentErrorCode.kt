package com.sclass.domain.domains.payment.exception

import com.sclass.common.exception.ErrorCode

enum class PaymentErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    PAYMENT_NOT_FOUND("PAYMENT_001", "결제 정보를 찾을 수 없습니다", 404),
    DUPLICATE_PG_ORDER("PAYMENT_002", "이미 처리된 주문입니다", 409),
    INVALID_PAYMENT_STATUS("PAYMENT_003", "유효하지 않은 결제 상태입니다", 400),
}
