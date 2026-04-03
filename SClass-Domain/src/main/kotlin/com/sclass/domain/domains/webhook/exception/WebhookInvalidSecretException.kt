package com.sclass.domain.domains.webhook.exception

import com.sclass.common.exception.BusinessException

class WebhookInvalidSecretException : BusinessException(WebhookErrorCode.WEBHOOK_INVALID_SECRET)
