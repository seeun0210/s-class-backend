package com.sclass.backoffice.partnership.dto

import com.sclass.domain.domains.partnership.domain.PartnershipLead
import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import java.time.LocalDateTime

data class PartnershipLeadDetailResponse(
    val id: Long,
    val academyName: String,
    val phone: String,
    val email: String?,
    val message: String?,
    val status: PartnershipLeadStatus,
    val note: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(lead: PartnershipLead) =
            PartnershipLeadDetailResponse(
                id = lead.id,
                academyName = lead.academyName,
                phone = lead.phone,
                email = lead.email,
                message = lead.message,
                status = lead.status,
                note = lead.note,
                createdAt = lead.createdAt,
                updatedAt = lead.updatedAt,
            )
    }
}
