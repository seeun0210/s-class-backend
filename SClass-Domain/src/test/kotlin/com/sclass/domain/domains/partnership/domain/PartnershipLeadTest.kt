package com.sclass.domain.domains.partnership.domain

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PartnershipLeadTest {
    private fun lead(
        status: PartnershipLeadStatus = PartnershipLeadStatus.NEW,
        note: String? = null,
    ) = PartnershipLead(
        academyName = "서울학원",
        phone = "01012345678",
        email = "foo@example.com",
        message = "도입 문의드립니다",
        status = status,
        note = note,
    )

    @Nested
    inner class UpdateStatus {
        @Test
        fun `status를 변경하고 note를 함께 업데이트한다`() {
            val lead = lead()

            lead.updateStatus(PartnershipLeadStatus.CONTACTED, "2026-04-17 10:00 통화 완료")

            assertAll(
                { assertEquals(PartnershipLeadStatus.CONTACTED, lead.status) },
                { assertEquals("2026-04-17 10:00 통화 완료", lead.note) },
            )
        }

        @Test
        fun `note가 null이면 기존 note는 유지된다`() {
            val lead = lead(note = "기존 메모")

            lead.updateStatus(PartnershipLeadStatus.COMPLETED, null)

            assertAll(
                { assertEquals(PartnershipLeadStatus.COMPLETED, lead.status) },
                { assertEquals("기존 메모", lead.note) },
            )
        }

        @Test
        fun `note가 null이고 기존 note도 null이면 null을 유지한다`() {
            val lead = lead()

            lead.updateStatus(PartnershipLeadStatus.REJECTED, null)

            assertAll(
                { assertEquals(PartnershipLeadStatus.REJECTED, lead.status) },
                { assertNull(lead.note) },
            )
        }
    }
}
