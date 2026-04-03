package com.sclass.domain.domains.webhook.exception

import com.sclass.common.exception.BusinessException

class WebhookInactiveException : BusinessException(WebhookErrorCode.WEBHOOK_INACTIVE)
