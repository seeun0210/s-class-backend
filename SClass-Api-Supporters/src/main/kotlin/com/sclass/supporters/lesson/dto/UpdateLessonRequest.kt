package com.sclass.supporters.lesson.dto

import java.time.LocalDateTime

data class UpdateLessonRequest(
    val name: String? = null,
    val scheduledAt: LocalDateTime? = null,
)
