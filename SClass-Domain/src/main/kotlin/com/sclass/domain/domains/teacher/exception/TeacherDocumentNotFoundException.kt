package com.sclass.domain.domains.teacher.exception

import com.sclass.common.exception.BusinessException

class TeacherDocumentNotFoundException : BusinessException(TeacherErrorCode.TEACHER_DOCUMENT_NOT_FOUND)
