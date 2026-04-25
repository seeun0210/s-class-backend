package com.sclass.supporters.lesson.dto

import java.time.LocalDateTime

data class StartLessonRequest(
    val startedAt: LocalDateTime? = null,
)
