package com.sclass.supporters.inquiry.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanStatus
import com.sclass.infrastructure.report.ReportServiceClient
import com.sclass.supporters.inquiry.dto.InquiryPlanDetailResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetInquiryPlanUseCase(
    private val inquiryPlanAdaptor: InquiryPlanAdaptor,
    private val reportServiceClient: ReportServiceClient,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        id: Long,
    ): InquiryPlanDetailResponse {
        val plan = inquiryPlanAdaptor.findByIdAndUserId(id, userId)
        val report =
            if (plan.status == InquiryPlanStatus.READY && plan.externalPlanId != null) {
                reportServiceClient.getReportByJobId(plan.externalPlanId!!)
            } else {
                null
            }
        return InquiryPlanDetailResponse.from(plan, report)
    }
}
