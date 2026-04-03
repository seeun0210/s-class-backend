package com.sclass.domain.domains.webhook.exception

import com.sclass.common.exception.BusinessException

class WebhookNotFoundException : BusinessException(WebhookErrorCode.WEBHOOK_NOT_FOUND)
