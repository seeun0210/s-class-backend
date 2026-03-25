package com.sclass.domain.domains.teacher.exception

import com.sclass.common.exception.BusinessException

class TeacherNotFoundException : BusinessException(TeacherErrorCode.TEACHER_NOT_FOUND)
