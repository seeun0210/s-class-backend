package com.sclass.supporters.commission.dto

import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.OutputFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateCommissionRequest(
    @field:NotBlank
    val teacherUserId: String,

    @field:NotNull
    val outputFormat: OutputFormat,

    @field:NotNull
    val activityType: ActivityType,

    @field:Valid
    @field:NotNull
    val guideInfo: GuideInfoRequest,

    val fileIds: List<String>? = null,
)

data class GuideInfoRequest(
    @field:NotBlank
    val subject: String,

    @field:NotBlank
    val volume: String,

    val requiredElements: String? = null,

    @field:NotBlank
    val gradingCriteria: String,

    @field:NotBlank
    val teacherEmphasis: String,
)
