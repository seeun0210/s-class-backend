package com.sclass.domain.domains.organization.exception

import com.sclass.common.exception.BusinessException

class OrganizationNotFoundException : BusinessException(OrganizationErrorCode.ORGANIZATION_NOT_FOUND)
