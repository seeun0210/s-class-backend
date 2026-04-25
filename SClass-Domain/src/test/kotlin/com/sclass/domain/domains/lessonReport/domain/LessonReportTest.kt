package com.sclass.domain.domains.lessonReport.domain

import com.sclass.domain.domains.lessonReport.exception.LessonReportInvalidStatusTransitionException
import com.sclass.domain.domains.lessonReport.exception.LessonReportNotRejectedException
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class LessonReportTest {
    private val reviewer = "reviewer-user-id-0000000001"
    private val submitter = "submitter-user-id-000000001"

    private fun newReport(status: LessonReportStatus = LessonReportStatus.PENDING_REVIEW) =
        LessonReport(
            lessonId = 1L,
            submittedByUserId = submitter,
            content = "content",
            status = status,
        )

    @Test
    fun `PENDING_REVIEW에서 approve 호출 시 APPROVED로 전이`() {
        val report = newReport()
        val now = LocalDateTime.now()
        report.approve(reviewer, now)
        assertAll(
            { assertEquals(LessonReportStatus.APPROVED, report.status) },
            { assertEquals(reviewer, report.reviewedByUserId) },
            { assertEquals(now, report.reviewedAt) },
        )
    }

    @Test
    fun `PENDING_REVIEW에서 reject 호출 시 REJECTED로 전이`() {
        val report = newReport()
        val now = LocalDateTime.now()
        report.reject(reviewer, "부족함", now)
        assertAll(
            { assertEquals(LessonReportStatus.REJECTED, report.status) },
            { assertEquals(reviewer, report.reviewedByUserId) },
            { assertEquals(now, report.reviewedAt) },
            { assertEquals("부족함", report.rejectReason) },
        )
    }

    @Test
    fun `APPROVED 상태에서 다시 approve 시 예외`() {
        val report = newReport(status = LessonReportStatus.APPROVED)
        assertThrows<LessonReportInvalidStatusTransitionException> {
            report.approve(reviewer)
        }
    }

    @Test
    fun `REJECTED 상태에서 approve 시 예외`() {
        val report = newReport(status = LessonReportStatus.REJECTED)
        assertThrows<LessonReportInvalidStatusTransitionException> {
            report.approve(reviewer)
        }
    }

    @Test
    fun `REJECTED 상태에서 resubmit 호출 시 PENDING_REVIEW로 전이되고 검토 정보가 초기화된다`() {
        val report =
            newReport(status = LessonReportStatus.REJECTED).apply {
                reviewedByUserId = reviewer
                reviewedAt = LocalDateTime.now()
                rejectReason = "부족함"
            }

        report.resubmit("updated")

        assertAll(
            { assertEquals(LessonReportStatus.PENDING_REVIEW, report.status) },
            { assertEquals("updated", report.content) },
            { assertEquals(null, report.reviewedByUserId) },
            { assertEquals(null, report.reviewedAt) },
            { assertEquals(null, report.rejectReason) },
        )
    }

    @Test
    fun `REJECTED가 아닌 상태에서 resubmit 호출 시 예외`() {
        val report = newReport()

        assertThrows<LessonReportNotRejectedException> {
            report.resubmit("updated")
        }
    }
}
