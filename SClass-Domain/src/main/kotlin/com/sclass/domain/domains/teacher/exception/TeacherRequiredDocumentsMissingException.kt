package com.sclass.domain.domains.teacher.exception

import com.sclass.common.exception.BusinessException

class TeacherRequiredDocumentsMissingException : BusinessException(TeacherErrorCode.TEACHER_REQUIRED_DOCUMENTS_MISSING)
