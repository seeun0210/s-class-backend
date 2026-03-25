package com.sclass.backoffice.organization.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateOrganizationRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val name: String,

    @field:NotBlank
    @field:Size(max = 50)
    val domain: String,

    @field:Size(max = 500)
    val logoUrl: String? = null,
)
