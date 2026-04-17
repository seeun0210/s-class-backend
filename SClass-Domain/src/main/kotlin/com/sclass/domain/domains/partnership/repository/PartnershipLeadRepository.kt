package com.sclass.domain.domains.partnership.repository

import com.sclass.domain.domains.partnership.domain.PartnershipLead
import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PartnershipLeadRepository : JpaRepository<PartnershipLead, Long> {
    fun existsByPhone(phone: String): Boolean

    fun findAllByStatus(
        status: PartnershipLeadStatus,
        pageable: Pageable,
    ): Page<PartnershipLead>
}
