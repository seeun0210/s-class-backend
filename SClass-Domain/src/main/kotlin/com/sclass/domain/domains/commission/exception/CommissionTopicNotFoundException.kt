package com.sclass.domain.domains.commission.exception

import com.sclass.common.exception.BusinessException

class CommissionTopicNotFoundException : BusinessException(CommissionErrorCode.COMMISSION_TOPIC_NOT_FOUND)
