package com.sclass.domain.domains.payment.exception

import com.sclass.common.exception.BusinessException

class PaymentUnauthorizedException : BusinessException(PaymentErrorCode.PAYMENT_UNAUTHORIZED)
