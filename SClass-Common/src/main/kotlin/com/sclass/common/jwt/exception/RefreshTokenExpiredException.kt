package com.sclass.common.jwt.exception

import com.sclass.common.exception.BusinessException

class RefreshTokenExpiredException private constructor() : BusinessException(TokenErrorCode.REFRESH_TOKEN_EXPIRED) {
    companion object {
        @JvmField
        val EXCEPTION: BusinessException = RefreshTokenExpiredException()
    }
}
