package com.sclass.domain.domains.coin.exception

import com.sclass.common.exception.BusinessException

class CoinPackageNotFoundException : BusinessException(CoinErrorCode.COIN_PACKAGE_NOT_FOUND)
