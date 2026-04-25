package com.sclass.supporters.lesson.dto

import java.time.LocalDateTime

data class CompleteLessonRequest(
    val completedAt: LocalDateTime? = null,
)
