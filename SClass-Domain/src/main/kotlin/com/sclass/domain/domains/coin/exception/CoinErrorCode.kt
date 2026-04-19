package com.sclass.domain.domains.coin.exception

import com.sclass.common.exception.ErrorCode

enum class CoinErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    INSUFFICIENT_COIN("COIN_002", "코인 잔액이 부족합니다", 400),
    COIN_PACKAGE_NOT_FOUND("COIN_003", "코인 패키지를 찾을 수 없습니다", 404),
    COIN_PACKAGE_INVALID_STATUS_TRANSITION("COIN_004", "잘못된 코인 패키지 상태 전이입니다", 400),
    INVALID_COIN_AMOUNT("COIN_005", "코인 금액은 1 이상이어야 합니다", 400),
}
