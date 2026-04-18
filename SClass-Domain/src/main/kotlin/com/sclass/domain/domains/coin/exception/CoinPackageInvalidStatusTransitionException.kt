package com.sclass.domain.domains.coin.exception

import com.sclass.common.exception.BusinessException

class CoinPackageInvalidStatusTransitionException : BusinessException(CoinErrorCode.COIN_PACKAGE_INVALID_STATUS_TRANSITION)
