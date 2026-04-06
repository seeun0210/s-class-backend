package com.sclass.common.jwt.exception

import com.sclass.common.exception.BusinessException

class RefreshTokenRevokedException : BusinessException(TokenErrorCode.REFRESH_TOKEN_REVOKED)
