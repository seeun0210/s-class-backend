package com.sclass.supporters.partnership.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.partnership.adaptor.PartnershipLeadAdaptor
import com.sclass.domain.domains.partnership.domain.PartnershipLead
import com.sclass.domain.domains.partnership.exception.PartnershipLeadAlreadyExistsException
import com.sclass.supporters.partnership.dto.CreatePartnershipLeadRequest
import com.sclass.supporters.partnership.dto.PartnershipLeadResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreatePartnershipLeadUseCase(
    private val partnershipLeadAdaptor: PartnershipLeadAdaptor,
) {
    @Transactional
    fun execute(request: CreatePartnershipLeadRequest): PartnershipLeadResponse {
        val normalizedPhone = request.phone.replace("-", "")
        if (partnershipLeadAdaptor.existsByPhone(normalizedPhone)) {
            throw PartnershipLeadAlreadyExistsException()
        }
        val lead =
            partnershipLeadAdaptor.save(
                PartnershipLead(
                    academyName = request.academyName,
                    phone = normalizedPhone,
                    email = request.email,
                    message = request.message,
                ),
            )
        return PartnershipLeadResponse(id = lead.id)
    }
}
