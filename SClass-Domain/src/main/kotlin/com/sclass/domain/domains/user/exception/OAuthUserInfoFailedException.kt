package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class OAuthUserInfoFailedException : BusinessException(OAuthErrorCode.OAUTH_USER_INFO_FAILED)
