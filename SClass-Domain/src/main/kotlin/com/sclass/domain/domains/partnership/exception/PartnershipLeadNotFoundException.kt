package com.sclass.domain.domains.partnership.exception

import com.sclass.common.exception.BusinessException

class PartnershipLeadNotFoundException : BusinessException(PartnershipLeadErrorCode.PARTNERSHIP_LEAD_NOT_FOUND)
