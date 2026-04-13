package com.sclass.domain.domains.lesson.exception

import com.sclass.common.exception.BusinessException

class LessonSubstituteSameAsAssignedException : BusinessException(LessonErrorCode.LESSON_SUBSTITUTE_SAME_AS_ASSIGNED)
