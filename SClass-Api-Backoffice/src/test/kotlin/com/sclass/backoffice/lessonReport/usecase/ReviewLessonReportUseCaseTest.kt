package com.sclass.backoffice.lessonReport.usecase

import com.sclass.backoffice.lessonReport.dto.ReviewDecision
import com.sclass.backoffice.lessonReport.dto.ReviewLessonReportRequest
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportAdaptor
import com.sclass.domain.domains.lessonReport.adaptor.LessonReportFileAdaptor
import com.sclass.domain.domains.lessonReport.domain.LessonReport
import com.sclass.domain.domains.lessonReport.domain.LessonReportStatus
import com.sclass.domain.domains.lessonReport.exception.LessonReportInvalidStatusTransitionException
import com.sclass.domain.domains.lessonReport.exception.LessonReportRejectReasonRequiredException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ReviewLessonReportUseCaseTest {
    private lateinit var lessonReportAdaptor: LessonReportAdaptor
    private lateinit var lessonReportFileAdaptor: LessonReportFileAdaptor
    private lateinit var useCase: ReviewLessonReportUseCase

    private val reviewer = "reviewer-user-id-0000000001"
    private val submitter = "submitter-user-id-000000001"

    @BeforeEach
    fun setUp() {
        lessonReportAdaptor = mockk()
        lessonReportFileAdaptor = mockk()
        useCase = ReviewLessonReportUseCase(lessonReportAdaptor, lessonReportFileAdaptor)
    }

    private fun newReport(status: LessonReportStatus = LessonReportStatus.PENDING_REVIEW) =
        LessonReport(
            id = 1L,
            lessonId = 1L,
            submittedByUserId = submitter,
            content = "c",
            status = status,
        )

    @Test
    fun `APPROVE 결정이면 report가 APPROVED로 전이`() {
        val report = newReport()
        every { lessonReportAdaptor.findById(1L) } returns report
        every { lessonReportFileAdaptor.findByLessonReportId(1L) } returns emptyList()

        val result = useCase.execute(reviewer, 1L, ReviewLessonReportRequest(ReviewDecision.APPROVE))

        assertAll(
            { assertEquals(LessonReportStatus.APPROVED, report.status) },
            { assertEquals(LessonReportStatus.APPROVED, result.status) },
            { assertEquals(reviewer, report.reviewedByUserId) },
        )
    }

    @Test
    fun `REJECT 결정 + reason이면 REJECTED로 전이`() {
        val report = newReport()
        every { lessonReportAdaptor.findById(1L) } returns report
        every { lessonReportFileAdaptor.findByLessonReportId(1L) } returns emptyList()

        useCase.execute(reviewer, 1L, ReviewLessonReportRequest(ReviewDecision.REJECT, rejectReason = "부족"))

        assertAll(
            { assertEquals(LessonReportStatus.REJECTED, report.status) },
            { assertEquals("부족", report.rejectReason) },
        )
    }

    @Test
    fun `REJECT인데 reason이 비어있으면 예외`() {
        val report = newReport()
        every { lessonReportAdaptor.findById(1L) } returns report

        assertThrows<LessonReportRejectReasonRequiredException> {
            useCase.execute(reviewer, 1L, ReviewLessonReportRequest(ReviewDecision.REJECT, rejectReason = "   "))
        }
    }

    @Test
    fun `이미 APPROVED된 리포트면 예외`() {
        val report = newReport(status = LessonReportStatus.APPROVED)
        every { lessonReportAdaptor.findById(1L) } returns report

        assertThrows<LessonReportInvalidStatusTransitionException> {
            useCase.execute(reviewer, 1L, ReviewLessonReportRequest(ReviewDecision.APPROVE))
        }
    }
}
