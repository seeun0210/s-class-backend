package com.sclass.domain.domains.coin.exception

import com.sclass.common.exception.BusinessException

class CoinBalanceNotFoundException : BusinessException(CoinErrorCode.COIN_BALANCE_NOT_FOUND)
