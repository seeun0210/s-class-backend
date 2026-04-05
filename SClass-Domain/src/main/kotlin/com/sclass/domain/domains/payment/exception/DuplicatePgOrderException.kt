package com.sclass.domain.domains.payment.exception

import com.sclass.common.exception.BusinessException

class DuplicatePgOrderException : BusinessException(PaymentErrorCode.DUPLICATE_PG_ORDER)
