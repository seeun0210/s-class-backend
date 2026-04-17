package com.sclass.backoffice.partnership.usecase

import com.sclass.backoffice.partnership.dto.PartnershipLeadDetailResponse
import com.sclass.backoffice.partnership.dto.UpdatePartnershipLeadStatusRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.partnership.adaptor.PartnershipLeadAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdatePartnershipLeadStatusUseCase(
    private val partnershipLeadAdaptor: PartnershipLeadAdaptor,
) {
    @Transactional
    fun execute(
        id: Long,
        request: UpdatePartnershipLeadStatusRequest,
    ): PartnershipLeadDetailResponse {
        val lead = partnershipLeadAdaptor.findById(id)
        lead.updateStatus(request.status, request.note)
        return PartnershipLeadDetailResponse.from(lead)
    }
}
