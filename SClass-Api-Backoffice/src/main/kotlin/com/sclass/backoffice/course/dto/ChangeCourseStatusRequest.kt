package com.sclass.backoffice.course.dto

import com.sclass.domain.domains.course.domain.CourseStatus
import jakarta.validation.constraints.NotNull

data class ChangeCourseStatusRequest(
    @field:NotNull
    val status: CourseStatus,
)
