package com.sclass.domain.domains.student.exception

import com.sclass.common.exception.BusinessException

class StudentNotFoundException : BusinessException(StudentErrorCode.STUDENT_NOT_FOUND)
