package com.sclass.domain.domains.teacherassignment.exception

import com.sclass.common.exception.BusinessException

class OrganizationNotAllowedForSupportersException :
    BusinessException(TeacherAssignmentErrorCode.ORGANIZATION_NOT_ALLOWED_FOR_SUPPORTERS)
