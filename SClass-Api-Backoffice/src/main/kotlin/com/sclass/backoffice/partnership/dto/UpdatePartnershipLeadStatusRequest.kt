package com.sclass.backoffice.partnership.dto

import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import jakarta.validation.constraints.Size

data class UpdatePartnershipLeadStatusRequest(
    val status: PartnershipLeadStatus,
    @field:Size(max = 2000)
    val note: String?,
)
