package com.sclass.domain.domains.lessonReport.domain

import com.sclass.domain.domains.lessonReport.exception.LessonReportInvalidStatusTransitionException
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
            version = 1,
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
}
