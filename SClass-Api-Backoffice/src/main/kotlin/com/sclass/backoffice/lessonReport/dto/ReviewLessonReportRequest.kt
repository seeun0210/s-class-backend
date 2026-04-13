package com.sclass.backoffice.lessonReport.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ReviewLessonReportRequest(
    @field:NotNull
    val decision: ReviewDecision,
    @field:Size(max = 1000)
    val rejectReason: String? = null,
)

enum class ReviewDecision {
    APPROVE,
    REJECT,
}
