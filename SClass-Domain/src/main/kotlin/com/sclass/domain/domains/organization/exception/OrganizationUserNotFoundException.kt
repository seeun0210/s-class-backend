package com.sclass.domain.domains.organization.exception

import com.sclass.common.exception.BusinessException

class OrganizationUserNotFoundException : BusinessException(OrganizationErrorCode.ORGANIZATION_USER_NOT_FOUND)
