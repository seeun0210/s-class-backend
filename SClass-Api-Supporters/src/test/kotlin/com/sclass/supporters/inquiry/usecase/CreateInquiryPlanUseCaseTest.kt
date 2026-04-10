package com.sclass.supporters.inquiry.usecase

import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanStatus
import com.sclass.infrastructure.report.ReportServiceClient
import com.sclass.supporters.inquiry.dto.CreateInquiryPlanRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateInquiryPlanUseCaseTest {
    private lateinit var inquiryPlanAdaptor: InquiryPlanAdaptor
    private lateinit var reportServiceClient: ReportServiceClient
    private lateinit var useCase: CreateInquiryPlanUseCase

    @BeforeEach
    fun setUp() {
        inquiryPlanAdaptor = mockk()
        reportServiceClient = mockk()
        useCase = CreateInquiryPlanUseCase(inquiryPlanAdaptor, reportServiceClient)
    }

    private fun request() =
        CreateInquiryPlanRequest(
            paragraph = "제로음료와 혈당의 관계",
            sourceType = InquiryPlanSourceType.LESSON,
            sourceRefId = 42L,
        )

    @Test
    fun `ReportService 호출 성공 시 jobId가 저장되고 PENDING 상태로 반환한다`() {
        val planSlot = slot<InquiryPlan>()
        every { inquiryPlanAdaptor.save(capture(planSlot)) } answers { planSlot.captured }
        every { reportServiceClient.createReport(any(), any()) } returns "job-abc"

        val result = useCase.execute("user-id-00000000001", request())

        assertAll(
            { assertEquals(InquiryPlanStatus.PENDING, result.status) },
            { assertEquals(InquiryPlanSourceType.LESSON, result.sourceType) },
            { assertEquals(42L, result.sourceRefId) },
        )
        assertEquals("job-abc", planSlot.captured.externalPlanId)
    }

    @Test
    fun `ReportService 호출 실패 시 FAILED 상태로 반환한다`() {
        val planSlot = slot<InquiryPlan>()
        every { inquiryPlanAdaptor.save(capture(planSlot)) } answers { planSlot.captured }
        every { reportServiceClient.createReport(any(), any()) } throws RuntimeException("timeout")

        val result = useCase.execute("user-id-00000000001", request())

        assertAll(
            { assertEquals(InquiryPlanStatus.FAILED, result.status) },
            { assertEquals("ReportService 호출 실패", planSlot.captured.failureReason) },
        )
    }

    @Test
    fun `ReportService에 올바른 paragraph를 전달한다`() {
        val planSlot = slot<InquiryPlan>()
        every { inquiryPlanAdaptor.save(capture(planSlot)) } answers { planSlot.captured }
        every { reportServiceClient.createReport(any(), any()) } returns "job-xyz"

        useCase.execute("user-id-00000000001", request())

        verify { reportServiceClient.createReport(any(), "제로음료와 혈당의 관계") }
    }
}
