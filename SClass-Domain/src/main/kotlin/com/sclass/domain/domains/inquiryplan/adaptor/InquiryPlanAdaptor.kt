package com.sclass.domain.domains.inquiryplan.adaptor

import com.sclass.common.annotation.Adaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.inquiryplan.exception.InquiryPlanNotFoundException
import com.sclass.domain.domains.inquiryplan.repository.InquiryPlanRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Adaptor
class InquiryPlanAdaptor(
    private val repository: InquiryPlanRepository,
) {
    fun save(inquiryPlan: InquiryPlan): InquiryPlan = repository.save(inquiryPlan)

    fun findById(id: Long): InquiryPlan = repository.findById(id).orElseThrow { InquiryPlanNotFoundException() }

    fun findByIdAndUserId(
        id: Long,
        userId: String,
    ): InquiryPlan =
        repository.findByIdAndRequestedByUserId(id, userId)
            ?: throw InquiryPlanNotFoundException()

    fun findByJobIdOrNull(jobId: String): InquiryPlan? = repository.findByExternalPlanId(jobId)

    fun findAllBySource(
        sourceType: InquiryPlanSourceType,
        sourceRefId: Long,
        pageable: Pageable,
    ): Page<InquiryPlan> = repository.findAllBySourceTypeAndSourceRefId(sourceType, sourceRefId, pageable)
}
