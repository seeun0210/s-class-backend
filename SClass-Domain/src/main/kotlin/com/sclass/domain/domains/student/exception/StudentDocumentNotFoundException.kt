package com.sclass.domain.domains.student.exception

import com.sclass.common.exception.BusinessException

class StudentDocumentNotFoundException : BusinessException(StudentErrorCode.STUDENT_DOCUMENT_NOT_FOUND)
