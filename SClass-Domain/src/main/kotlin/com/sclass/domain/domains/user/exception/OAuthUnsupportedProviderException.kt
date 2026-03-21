package com.sclass.domain.domains.user.exception

import com.sclass.common.exception.BusinessException

class OAuthUnsupportedProviderException : BusinessException(OAuthErrorCode.OAUTH_UNSUPPORTED_PROVIDER)
