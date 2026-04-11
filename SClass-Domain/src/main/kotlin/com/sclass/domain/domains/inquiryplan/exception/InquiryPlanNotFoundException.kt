package com.sclass.domain.domains.inquiryplan.exception

import com.sclass.common.exception.BusinessException

class InquiryPlanNotFoundException : BusinessException(InquiryPlanErrorCode.INQUIRY_PLAN_NOT_FOUND)
