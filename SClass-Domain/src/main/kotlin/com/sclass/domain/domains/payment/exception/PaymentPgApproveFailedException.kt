package com.sclass.domain.domains.payment.exception

import com.sclass.common.exception.BusinessException

class PaymentPgApproveFailedException : BusinessException(PaymentErrorCode.PAYMENT_PG_APPROVE_FAILED)
