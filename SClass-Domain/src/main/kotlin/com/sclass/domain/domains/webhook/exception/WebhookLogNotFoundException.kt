package com.sclass.domain.domains.webhook.exception

import com.sclass.common.exception.BusinessException

class WebhookLogNotFoundException : BusinessException(WebhookErrorCode.WEBHOOK_LOG_NOT_FOUND)
