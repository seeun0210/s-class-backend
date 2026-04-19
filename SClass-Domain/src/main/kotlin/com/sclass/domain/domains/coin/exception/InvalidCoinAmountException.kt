package com.sclass.domain.domains.coin.exception

import com.sclass.common.exception.BusinessException

class InvalidCoinAmountException : BusinessException(CoinErrorCode.INVALID_COIN_AMOUNT)
