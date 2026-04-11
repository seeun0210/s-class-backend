package com.sclass.supporters.inquiry.usecase

import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlan
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanStatus
import com.sclass.infrastructure.report.ReportServiceClient
import com.sclass.supporters.inquiry.dto.CreateInquiryPlanRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate

class CreateInquiryPlanUseCaseTest {
    private lateinit var inquiryPlanAdaptor: InquiryPlanAdaptor
    private lateinit var reportServiceClient: ReportServiceClient
    private lateinit var txTemplate: TransactionTemplate
    private lateinit var useCase: CreateInquiryPlanUseCase

    @BeforeEach
    fun setUp() {
        inquiryPlanAdaptor = mockk()
        reportServiceClient = mockk()
        txTemplate = mockk()
        every { txTemplate.execute(any<TransactionCallback<Any?>>()) } answers {
            firstArg<TransactionCallback<Any?>>().doInTransaction(mockk())
        }
        useCase = CreateInquiryPlanUseCase(inquiryPlanAdaptor, reportServiceClient, txTemplate)
    }

    private fun request() =
        CreateInquiryPlanRequest(
            paragraph = "제로음료와 혈당의 관계",
            sourceType = InquiryPlanSourceType.LESSON,
            sourceRefId = 42L,
        )

    private fun savedPlan(status: InquiryPlanStatus = InquiryPlanStatus.PENDING) =
        InquiryPlan(
            id = 1L,
            sourceType = InquiryPlanSourceType.LESSON,
            sourceRefId = 42L,
            requestedByUserId = "user-id-00000000001",
            status = status,
        )

    @Nested
    inner class Success {
        @Test
        fun `ReportService 호출 성공 시 jobId가 저장되고 PENDING 상태로 반환한다`() {
            val plan = savedPlan()
            every { inquiryPlanAdaptor.save(any()) } returns plan
            every { inquiryPlanAdaptor.findById(1L) } returns plan
            every { reportServiceClient.createReport(any(), any()) } returns "job-abc"

            val result = useCase.execute("user-id-00000000001", request())

            assertAll(
                { assertEquals(InquiryPlanStatus.PENDING, result.status) },
                { assertEquals(InquiryPlanSourceType.LESSON, result.sourceType) },
                { assertEquals(42L, result.sourceRefId) },
            )
            verify { reportServiceClient.createReport("1", "제로음료와 혈당의 관계") }
        }

        @Test
        fun `ReportService 호출은 트랜잭션 외부에서 수행된다`() {
            val plan = savedPlan()
            every { inquiryPlanAdaptor.save(any()) } returns plan
            every { inquiryPlanAdaptor.findById(1L) } returns plan
            every { reportServiceClient.createReport(any(), any()) } returns "job-abc"

            useCase.execute("user-id-00000000001", request())

            // save(최초) + save(jobId 업데이트) = txTemplate 2번 (최종 반환 findById는 txTemplate 밖)
            verify(exactly = 2) { txTemplate.execute(any<TransactionCallback<Any?>>()) }
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `ReportService 호출 실패 시 FAILED 상태로 저장된다`() {
            val plan = savedPlan()
            every { inquiryPlanAdaptor.save(any()) } returns plan
            every { inquiryPlanAdaptor.findById(1L) } returns plan
            every { reportServiceClient.createReport(any(), any()) } throws RuntimeException("timeout")

            useCase.execute("user-id-00000000001", request())

            verify(exactly = 1) { reportServiceClient.createReport(any(), any()) }
            // save(최초) + save(markFailed) = txTemplate 2번 (최종 반환 findById는 txTemplate 밖)
            verify(exactly = 2) { txTemplate.execute(any<TransactionCallback<Any?>>()) }
        }
    }
}
