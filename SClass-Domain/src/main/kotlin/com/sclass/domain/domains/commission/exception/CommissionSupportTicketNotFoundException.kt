package com.sclass.domain.domains.commission.exception

import com.sclass.common.exception.BusinessException

class CommissionSupportTicketNotFoundException : BusinessException(CommissionErrorCode.SUPPORT_TICKET_NOT_FOUND)
