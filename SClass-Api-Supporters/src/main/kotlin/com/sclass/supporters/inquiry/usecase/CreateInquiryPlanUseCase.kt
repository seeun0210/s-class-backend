package com.sclass.supporters.inquiry.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.infrastructure.report.ReportServiceClient
import com.sclass.supporters.inquiry.dto.CreateInquiryPlanRequest
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateInquiryPlanUseCase(
    private val inquiryPlanAdaptor: InquiryPlanAdaptor,
    private val reportServiceClient: ReportServiceClient,
) {
    @Transactional
    fun execute(
        userId: String,
        request: CreateInquiryPlanRequest,
    ): InquiryPlanResponse {
        val plan =
            inquiryPlanAdaptor.save(
                InquiryPlan(
                    sourceType = request.sourceType,
                    sourceRefId = request.sourceRefId,
                    requestedByUserId = userId,
                ),
            )
        runCatching {
            val jobId = reportServiceClient.createReport(plan.id.toString(), request.paragraph)
            plan.acceptJobId(jobId)
        }.onFailure {
            logger.error("[inquiry] ReportService 호출 실패 planId=${plan.id}", it)
            plan.markFailed("ReportService 호출 실패")
        }
        return InquiryPlanResponse.from(plan)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateInquiryPlanUseCase::class.java)
    }
}
