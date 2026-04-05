package com.sclass.domain.domains.coin.exception

import com.sclass.common.exception.BusinessException

class InsufficientCoinException : BusinessException(CoinErrorCode.INSUFFICIENT_COIN)
