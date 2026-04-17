package com.sclass.backoffice.partnership.usecase

import com.sclass.backoffice.partnership.dto.PartnershipLeadDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.partnership.adaptor.PartnershipLeadAdaptor
import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetPartnershipLeadsUseCase(
    private val partnershipLeadAdaptor: PartnershipLeadAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        status: PartnershipLeadStatus?,
        pageable: Pageable,
    ): Page<PartnershipLeadDetailResponse> {
        val page =
            if (status == null) {
                partnershipLeadAdaptor.findAll(pageable)
            } else {
                partnershipLeadAdaptor.findAllByStatus(status, pageable)
            }
        return page.map { PartnershipLeadDetailResponse.from(it) }
    }
}
