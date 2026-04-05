package com.sclass.domain.domains.payment.exception

import com.sclass.common.exception.BusinessException

class PaymentNotFoundException : BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND)
