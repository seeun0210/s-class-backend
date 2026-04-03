package com.sclass.domain.domains.diagnosis.exception

import com.sclass.common.exception.BusinessException

class DiagnosisNotFoundException : BusinessException(DiagnosisErrorCode.DIAGNOSIS_NOT_FOUND)
