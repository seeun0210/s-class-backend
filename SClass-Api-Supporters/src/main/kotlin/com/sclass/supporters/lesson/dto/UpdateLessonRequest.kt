package com.sclass.supporters.lesson.dto

import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UpdateLessonRequest(
    @field:Size(max = 200) val name: String? = null,
    val scheduledAt: LocalDateTime? = null,
)
