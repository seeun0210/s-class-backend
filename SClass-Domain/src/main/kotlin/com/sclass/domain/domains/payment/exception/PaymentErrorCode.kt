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
    PAYMENT_PG_APPROVE_FAILED("PAYMENT_004", "PG 결제 승인에 실패했습니다", 502),
    PAYMENT_PG_CANCEL_FAILED("PAYMENT_005", "PG 결제 취소에 실패했습니다", 502),
    PAYMENT_UNAUTHORIZED("PAYMENT_006", "본인의 결제 정보만 접근할 수 있습니다", 403),
}
