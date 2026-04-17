package com.sclass.domain.domains.commission.exception

import com.sclass.common.exception.ErrorCode

enum class CommissionErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    COMMISSION_NOT_FOUND("COMMISSION_001", "의뢰를 찾을 수 없습니다", 404),
    INVALID_STATUS_TRANSITION("COMMISSION_002", "유효하지 않은 상태 변경입니다", 400),
    UNAUTHORIZED_ACCESS("COMMISSION_003", "해당 의뢰에 대한 권한이 없습니다", 403),
    COMMISSION_TOPIC_NOT_FOUND("COMMISSION_004", "추천 주제를 찾을 수 없습니다", 404),
    SUPPORT_TICKET_NOT_FOUND("COMMISSION_005", "지원 티켓을 찾을 수 없습니다", 404),
    COMMISSION_POLICY_NOT_CONFIGURED("COMMISSION_006", "활성화된 의뢰 정책이 없습니다", 500),
}
