package com.sclass.backoffice.partnership.usecase

import com.sclass.backoffice.partnership.dto.UpdatePartnershipLeadStatusRequest
import com.sclass.domain.domains.partnership.adaptor.PartnershipLeadAdaptor
import com.sclass.domain.domains.partnership.domain.PartnershipLead
import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import com.sclass.domain.domains.partnership.exception.PartnershipLeadNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdatePartnershipLeadStatusUseCaseTest {
    private lateinit var partnershipLeadAdaptor: PartnershipLeadAdaptor
    private lateinit var useCase: UpdatePartnershipLeadStatusUseCase

    @BeforeEach
    fun setUp() {
        partnershipLeadAdaptor = mockk()
        useCase = UpdatePartnershipLeadStatusUseCase(partnershipLeadAdaptor)
    }

    private fun lead() =
        PartnershipLead(
            id = 1L,
            academyName = "서울학원",
            phone = "01012345678",
            email = null,
            message = null,
            status = PartnershipLeadStatus.NEW,
        )

    @Nested
    inner class Success {
        @Test
        fun `status와 note를 함께 업데이트한다`() {
            val lead = lead()
            every { partnershipLeadAdaptor.findById(1L) } returns lead

            val result =
                useCase.execute(
                    1L,
                    UpdatePartnershipLeadStatusRequest(
                        status = PartnershipLeadStatus.CONTACTED,
                        note = "통화 완료",
                    ),
                )

            assertAll(
                { assertEquals(PartnershipLeadStatus.CONTACTED, result.status) },
                { assertEquals("통화 완료", result.note) },
                { assertEquals(PartnershipLeadStatus.CONTACTED, lead.status) },
                { assertEquals("통화 완료", lead.note) },
            )
        }

        @Test
        fun `note가 null이면 status만 변경되고 기존 note는 유지된다`() {
            val lead = lead().apply { updateStatus(PartnershipLeadStatus.CONTACTED, "기존 메모") }
            every { partnershipLeadAdaptor.findById(1L) } returns lead

            val result =
                useCase.execute(
                    1L,
                    UpdatePartnershipLeadStatusRequest(
                        status = PartnershipLeadStatus.COMPLETED,
                        note = null,
                    ),
                )

            assertAll(
                { assertEquals(PartnershipLeadStatus.COMPLETED, result.status) },
                { assertEquals("기존 메모", result.note) },
            )
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `존재하지 않는 id면 PartnershipLeadNotFoundException을 던진다`() {
            every { partnershipLeadAdaptor.findById(99L) } throws PartnershipLeadNotFoundException()

            assertThrows<PartnershipLeadNotFoundException> {
                useCase.execute(
                    99L,
                    UpdatePartnershipLeadStatusRequest(
                        status = PartnershipLeadStatus.CONTACTED,
                        note = null,
                    ),
                )
            }
        }
    }
}
