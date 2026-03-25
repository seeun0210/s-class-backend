package com.sclass.backoffice.organization.dto

import jakarta.validation.constraints.NotNull

data class UpdateOrganizationSettingsRequest(
    @field:NotNull val useSupporters: Boolean,
    @field:NotNull val useLms: Boolean,
)
