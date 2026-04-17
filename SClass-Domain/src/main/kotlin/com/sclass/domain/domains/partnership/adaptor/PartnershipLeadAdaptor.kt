package com.sclass.domain.domains.partnership.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.partnership.domain.PartnershipLead
import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import com.sclass.domain.domains.partnership.exception.PartnershipLeadNotFoundException
import com.sclass.domain.domains.partnership.repository.PartnershipLeadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class PartnershipLeadAdaptor(
    private val partnershipLeadRepository: PartnershipLeadRepository,
) {
    fun findById(id: Long): PartnershipLead = partnershipLeadRepository.findById(id).orElseThrow { PartnershipLeadNotFoundException() }

    fun existsByPhone(phone: String): Boolean = partnershipLeadRepository.existsByPhone(phone)

    fun findAll(pageable: Pageable): Page<PartnershipLead> = partnershipLeadRepository.findAll(pageable)

    fun findAllByStatus(
        status: PartnershipLeadStatus,
        pageable: Pageable,
    ): Page<PartnershipLead> = partnershipLeadRepository.findAllByStatus(status, pageable)

    fun save(lead: PartnershipLead): PartnershipLead = partnershipLeadRepository.save(lead)
}
