package com.sclass.common.jwt.exception

import com.sclass.common.exception.BusinessException

class RefreshTokenExpiredException : BusinessException(TokenErrorCode.REFRESH_TOKEN_EXPIRED)
