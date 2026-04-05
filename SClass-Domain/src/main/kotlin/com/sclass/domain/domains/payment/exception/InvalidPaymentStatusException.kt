package com.sclass.domain.domains.payment.exception

import com.sclass.common.exception.BusinessException

class InvalidPaymentStatusException : BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS)
