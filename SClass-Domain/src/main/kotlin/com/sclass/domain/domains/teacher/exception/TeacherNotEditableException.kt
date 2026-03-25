package com.sclass.domain.domains.teacher.exception

import com.sclass.common.exception.BusinessException

class TeacherNotEditableException : BusinessException(TeacherErrorCode.TEACHER_NOT_EDITABLE)
