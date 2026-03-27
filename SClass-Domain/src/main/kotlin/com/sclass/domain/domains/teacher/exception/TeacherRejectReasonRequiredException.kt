package com.sclass.domain.domains.teacher.exception

import com.sclass.common.exception.BusinessException

class TeacherRejectReasonRequiredException : BusinessException(TeacherErrorCode.TEACHER_REJECT_REASON_REQUIRED)
