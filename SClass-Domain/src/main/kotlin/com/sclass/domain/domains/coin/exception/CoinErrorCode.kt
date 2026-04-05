package com.sclass.domain.domains.coin.exception

import com.sclass.common.exception.ErrorCode

enum class CoinErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: Int,
) : ErrorCode {
    COIN_BALANCE_NOT_FOUND("COIN_001", "코인 잔액 정보를 찾을 수 없습니다", 404),
    INSUFFICIENT_COIN("COIN_002", "코인 잔액이 부족합니다", 400),
}
