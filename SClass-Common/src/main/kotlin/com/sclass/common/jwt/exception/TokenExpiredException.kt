package com.sclass.common.jwt.exception

import com.sclass.common.exception.BusinessException

class TokenExpiredException private constructor() : BusinessException(TokenErrorCode.TOKEN_EXPIRED) {
    companion object {
        @JvmField
        val EXCEPTION: BusinessException = TokenExpiredException()
    }
}
