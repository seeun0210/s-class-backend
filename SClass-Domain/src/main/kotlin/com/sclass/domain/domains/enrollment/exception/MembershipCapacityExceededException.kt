package com.sclass.domain.domains.enrollment.exception

import com.sclass.common.exception.BusinessException

class MembershipCapacityExceededException : BusinessException(EnrollmentErrorCode.MEMBERSHIP_CAPACITY_EXCEEDED)
