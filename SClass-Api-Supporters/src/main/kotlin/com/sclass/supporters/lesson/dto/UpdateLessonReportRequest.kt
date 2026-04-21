package com.sclass.supporters.lesson.dto

import jakarta.validation.constraints.NotBlank

data class UpdateLessonReportRequest(
    @field:NotBlank
    val content: String,
    val fileIds: List<String> = emptyList(),
)
