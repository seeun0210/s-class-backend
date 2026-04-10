package com.sclass.domain.domains.inquiryplan.domain

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InquiryPlanTest {
    private fun plan() =
        InquiryPlan(
            sourceType = InquiryPlanSourceType.LESSON,
            sourceRefId = 1L,
            requestedByUserId = "user-id-00000000001",
        )

    @Test
    fun `acceptJobId는 PENDING 상태에서 externalPlanId를 설정한다`() {
        val p = plan()
        p.acceptJobId("job-123")
        assertEquals("job-123", p.externalPlanId)
        assertEquals(InquiryPlanStatus.PENDING, p.status)
    }

    @Test
    fun `markReady는 PENDING 상태에서 topic을 설정하고 READY로 전환한다`() {
        val p = plan()
        p.markReady("제로음료와 혈당의 관계")
        assertAll(
            { assertEquals(InquiryPlanStatus.READY, p.status) },
            { assertEquals("제로음료와 혈당의 관계", p.topic) },
        )
    }

    @Test
    fun `markReady는 topic이 null이어도 READY로 전환한다`() {
        val p = plan()
        p.markReady(null)
        assertAll(
            { assertEquals(InquiryPlanStatus.READY, p.status) },
            { assertNull(p.topic) },
        )
    }

    @Test
    fun `markFailed는 PENDING 상태에서 failureReason을 설정하고 FAILED로 전환한다`() {
        val p = plan()
        p.markFailed("ReportService 호출 실패")
        assertAll(
            { assertEquals(InquiryPlanStatus.FAILED, p.status) },
            { assertEquals("ReportService 호출 실패", p.failureReason) },
        )
    }

    @Test
    fun `markReady는 PENDING이 아닌 상태에서 호출하면 예외가 발생한다`() {
        val p = plan()
        p.markFailed("오류")
        assertThrows<IllegalArgumentException> { p.markReady("토픽") }
    }

    @Test
    fun `markFailed는 PENDING이 아닌 상태에서 호출하면 예외가 발생한다`() {
        val p = plan()
        p.markReady("토픽")
        assertThrows<IllegalArgumentException> { p.markFailed("오류") }
    }
}
