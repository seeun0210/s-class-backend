package com.sclass.domain.domains.enrollment.exception

import com.sclass.common.exception.BusinessException

class EnrollmentUnauthorizedAccessException : BusinessException(EnrollmentErrorCode.ENROLLMENT_UNAUTHORIZED_ACCESS)
