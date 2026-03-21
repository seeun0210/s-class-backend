package com.sclass.common.jwt.exception

import com.sclass.common.exception.BusinessException

class TokenExpiredException : BusinessException(TokenErrorCode.TOKEN_EXPIRED)
