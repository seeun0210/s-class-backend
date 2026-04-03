package com.sclass.domain.domains.webhook.exception

import com.sclass.common.exception.ErrorCode

enum class WebhookErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    WEBHOOK_NOT_FOUND("WEBHOOK_001", "웹훅을 찾을 수 없습니다", 404),
    WEBHOOK_LOG_NOT_FOUND("WEBHOOK_002", "웹훅 로그를 찾을 수 없습니다", 404),
    WEBHOOK_INVALID_SECRET("WEBHOOK_003", "유효하지 않은 웹훅 시크릿입니다", 401),
    WEBHOOK_INACTIVE("WEBHOOK_004", "비활성화된 웹훅입니다", 400),
}
