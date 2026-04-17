package com.sclass.backoffice.partnership.dto

import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import jakarta.validation.constraints.Size
import software.amazon.awssdk.annotations.NotNull

data class UpdatePartnershipLeadStatusRequest(
    @field:NotNull
    val status: PartnershipLeadStatus,

    @field:Size(max = 2000)
    val note: String?,
)
