package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class UserNotFoundException private constructor() : BusinessException(UserErrorCode.USER_NOT_FOUND) {
    companion object {
        @JvmField
        val EXCEPTION: BusinessException = UserNotFoundException()
    }
}
