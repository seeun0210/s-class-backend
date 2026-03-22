package com.sclass.domain.domains.student.exception

import com.sclass.common.exception.BusinessException

class StudentAlreadyExistsException : BusinessException(StudentErrorCode.STUDENT_ALREADY_EXISTS)
