package com.sclass.backoffice.file.dto

import com.sclass.domain.domains.file.domain.FileType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class PresignedUrlRequest(
    @field:NotBlank
    val originalFilename: String,

    @field:NotNull
    val fileType: FileType,

    @field:NotBlank
    val contentType: String,

    @field:NotNull
    @field:Positive
    val fileSize: Long,
)
