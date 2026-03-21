package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class OAuthEmailNotProvidedException : BusinessException(OAuthErrorCode.OAUTH_EMAIL_NOT_PROVIDED)
