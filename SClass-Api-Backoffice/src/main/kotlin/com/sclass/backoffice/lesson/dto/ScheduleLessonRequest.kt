package com.sclass.backoffice.lesson.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ScheduleLessonRequest(
    @field:Size(max = 200)
    val name: String? = null,

    @field:NotNull
    val scheduledAt: LocalDateTime?,
)
