package com.sclass.supporters.inquiry.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.supporters.inquiry.dto.InquiryPlanStatusResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetInquiryPlanStatusUseCase(
    private val inquiryPlanAdaptor: InquiryPlanAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        id: Long,
    ): InquiryPlanStatusResponse {
        val plan = inquiryPlanAdaptor.findByIdAndUserId(id, userId)
        return InquiryPlanStatusResponse.from(plan)
    }
}
