package com.sclass.domain.domains.inquiryplan.repository

import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface InquiryPlanRepository : JpaRepository<InquiryPlan, Long> {
    fun findAllBySourceTypeAndSourceRefId(
        sourceType: InquiryPlanSourceType,
        sourceRefId: Long,
        pageable: Pageable,
    ): Page<InquiryPlan>

    fun findByIdAndRequestedByUserId(
        id: Long,
        requestedByUserId: String,
    ): InquiryPlan?

    fun findByExternalPlanId(externalPlanId: String): InquiryPlan?

    fun findFirstBySourceTypeAndSourceRefIdOrderByIdDesc(
        sourceType: InquiryPlanSourceType,
        sourceRefId: Long,
    ): InquiryPlan?

    fun findAllBySourceTypeAndSourceRefIdOrderByIdDesc(
        sourceType: InquiryPlanSourceType,
        sourceRefId: Long,
    ): List<InquiryPlan>

    fun findAllBySourceTypeAndSourceRefIdIn(
        sourceType: InquiryPlanSourceType,
        sourceRefIds: List<Long>,
    ): List<InquiryPlan>
}
