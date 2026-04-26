package com.sclass.domain.domains.oauth.exception

import com.sclass.common.exception.BusinessException

class TeacherGoogleAccountNotFoundException : BusinessException(OAuthErrorCode.TEACHER_GOOGLE_ACCOUNT_NOT_FOUND)
