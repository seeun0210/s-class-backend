package com.sclass.supporters.lesson.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class RecordLessonRequest(
    @field:NotNull val startedAt: LocalDateTime,
    @field:NotNull val completedAt: LocalDateTime,
)
