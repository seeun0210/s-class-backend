package com.sclass.supporters.inquiry.usecase

import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanStatus
import com.sclass.infrastructure.report.ReportServiceClient
import com.sclass.infrastructure.report.dto.ReportStateDto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetInquiryPlanUseCaseTest {
    private lateinit var inquiryPlanAdaptor: InquiryPlanAdaptor
    private lateinit var reportServiceClient: ReportServiceClient
    private lateinit var useCase: GetInquiryPlanUseCase

    @BeforeEach
    fun setUp() {
        inquiryPlanAdaptor = mockk()
        reportServiceClient = mockk()
        useCase = GetInquiryPlanUseCase(inquiryPlanAdaptor, reportServiceClient)
    }

    private fun readyPlan(jobId: String = "job-123") =
        InquiryPlan(
            id = 1L,
            sourceType = InquiryPlanSourceType.LESSON,
            sourceRefId = 10L,
            requestedByUserId = "user-id-00000000001",
            status = InquiryPlanStatus.READY,
            externalPlanId = jobId,
            topic = "제로음료와 혈당",
        )

    private fun pendingPlan() =
        InquiryPlan(
            id = 2L,
            sourceType = InquiryPlanSourceType.LESSON,
            sourceRefId = 10L,
            requestedByUserId = "user-id-00000000001",
            status = InquiryPlanStatus.PENDING,
        )

    private fun reportStateDto() =
        ReportStateDto(
            id = "mongo-id",
            jobId = "job-123",
            paragraph = "제로음료와 혈당의 관계",
            topic = "제로음료와 혈당",
        )

    @Test
    fun `READY 상태이고 jobId가 있으면 ReportService에서 보고서를 조회한다`() {
        every { inquiryPlanAdaptor.findByIdAndUserId(1L, any()) } returns readyPlan()
        every { reportServiceClient.getReportByJobId("job-123") } returns reportStateDto()

        val result = useCase.execute("user-id-00000000001", 1L)

        assertAll(
            { assertEquals(InquiryPlanStatus.READY, result.status) },
            { assertNotNull(result.report) },
            { assertEquals("job-123", result.report?.jobId) },
        )
        verify { reportServiceClient.getReportByJobId("job-123") }
    }

    @Test
    fun `PENDING 상태이면 ReportService를 호출하지 않고 report는 null이다`() {
        every { inquiryPlanAdaptor.findByIdAndUserId(2L, any()) } returns pendingPlan()

        val result = useCase.execute("user-id-00000000001", 2L)

        assertAll(
            { assertEquals(InquiryPlanStatus.PENDING, result.status) },
            { assertNull(result.report) },
        )
        verify(exactly = 0) { reportServiceClient.getReportByJobId(any()) }
    }
}
