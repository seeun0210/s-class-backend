package com.sclass.domain.domains.teacher.exception

import com.sclass.common.exception.BusinessException

class TeacherAlreadyExistsException : BusinessException(TeacherErrorCode.TEACHER_ALREADY_EXISTS)
