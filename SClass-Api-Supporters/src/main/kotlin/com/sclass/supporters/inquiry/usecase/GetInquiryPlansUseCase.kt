package com.sclass.supporters.inquiry.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetInquiryPlansUseCase(
    private val inquiryPlanAdaptor: InquiryPlanAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        sourceType: InquiryPlanSourceType,
        sourceRefId: Long,
        pageable: Pageable,
    ): Page<InquiryPlanResponse> =
        inquiryPlanAdaptor
            .findAllBySource(sourceType, sourceRefId, pageable)
            .map { InquiryPlanResponse.from(it) }
}
