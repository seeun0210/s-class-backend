package com.sclass.supporters.lesson.dto

import jakarta.validation.constraints.NotBlank

data class CreateLessonInquiryPlanRequest(
    @field:NotBlank val paragraph: String,
)
