package com.sclass.domain.domains.lesson.exception

import com.sclass.common.exception.BusinessException

class LessonUnauthorizedAccessException : BusinessException(LessonErrorCode.LESSON_UNAUTHORIZED_ACCESS)
