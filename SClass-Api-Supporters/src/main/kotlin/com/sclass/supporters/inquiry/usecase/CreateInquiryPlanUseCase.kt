package com.sclass.supporters.inquiry.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.infrastructure.report.ReportServiceClient
import com.sclass.supporters.inquiry.dto.CreateInquiryPlanRequest
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionTemplate

@UseCase
class CreateInquiryPlanUseCase(
    private val inquiryPlanAdaptor: InquiryPlanAdaptor,
    private val reportServiceClient: ReportServiceClient,
    private val txTemplate: TransactionTemplate,
) {
    fun execute(
        userId: String,
        request: CreateInquiryPlanRequest,
    ): InquiryPlanResponse {
        val plan =
            txTemplate.execute {
                inquiryPlanAdaptor.save(
                    InquiryPlan(
                        sourceType = request.sourceType,
                        sourceRefId = request.sourceRefId,
                        requestedByUserId = userId,
                    ),
                )
            }!!

        runCatching {
            val jobId = reportServiceClient.createReport(plan.id.toString(), request.paragraph)
            txTemplate.execute {
                val fresh = inquiryPlanAdaptor.findById(plan.id)
                fresh.acceptJobId(jobId)
                inquiryPlanAdaptor.save(fresh)
            }
        }.onFailure {
            logger.error("[inquiry] ReportService 호출 실패 planId=${plan.id}", it)
            txTemplate.execute {
                val fresh = inquiryPlanAdaptor.findById(plan.id)
                fresh.markFailed("ReportService 호출 실패")
                inquiryPlanAdaptor.save(fresh)
            }
        }

        return InquiryPlanResponse.from(inquiryPlanAdaptor.findById(plan.id))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateInquiryPlanUseCase::class.java)
    }
}
