package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class OAuthProviderException : BusinessException(OAuthErrorCode.OAUTH_PROVIDER_ERROR)
