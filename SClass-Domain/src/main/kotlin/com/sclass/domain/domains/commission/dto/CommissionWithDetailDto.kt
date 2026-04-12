package com.sclass.domain.domains.commission.dto

import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.lesson.domain.Lesson

data class CommissionWithDetailDto(
    val commission: Commission,
    val studentName: String,
    val teacherName: String,
    val lesson: Lesson?,
)
