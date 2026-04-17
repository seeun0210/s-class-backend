package com.sclass.supporters.partnership.usecase

import com.sclass.domain.domains.partnership.adaptor.PartnershipLeadAdaptor
import com.sclass.domain.domains.partnership.domain.PartnershipLead
import com.sclass.domain.domains.partnership.exception.PartnershipLeadAlreadyExistsException
import com.sclass.supporters.partnership.dto.CreatePartnershipLeadRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreatePartnershipLeadUseCaseTest {
    private lateinit var partnershipLeadAdaptor: PartnershipLeadAdaptor
    private lateinit var useCase: CreatePartnershipLeadUseCase

    @BeforeEach
    fun setUp() {
        partnershipLeadAdaptor = mockk()
        useCase = CreatePartnershipLeadUseCase(partnershipLeadAdaptor)
    }

    private fun request(
        phone: String = "010-1234-5678",
        email: String? = "foo@example.com",
        message: String? = "도입 문의",
    ) = CreatePartnershipLeadRequest(
        academyName = "서울학원",
        phone = phone,
        email = email,
        message = message,
    )

    @Nested
    inner class Success {
        @Test
        fun `하이픈이 포함된 전화번호는 하이픈을 제거하고 저장한다`() {
            val savedSlot = slot<PartnershipLead>()
            every { partnershipLeadAdaptor.existsByPhone("01012345678") } returns false
            every { partnershipLeadAdaptor.save(capture(savedSlot)) } answers { savedSlot.captured }

            useCase.execute(request(phone = "010-1234-5678"))

            verify { partnershipLeadAdaptor.existsByPhone("01012345678") }
            assertEquals("01012345678", savedSlot.captured.phone)
        }

        @Test
        fun `저장된 엔티티의 id를 응답으로 반환한다`() {
            val lead =
                PartnershipLead(
                    id = 42L,
                    academyName = "서울학원",
                    phone = "01012345678",
                    email = "foo@example.com",
                    message = "도입 문의",
                )
            every { partnershipLeadAdaptor.existsByPhone(any()) } returns false
            every { partnershipLeadAdaptor.save(any()) } returns lead

            val result = useCase.execute(request())

            assertEquals(42L, result.id)
        }

        @Test
        fun `email과 message는 null 가능하다`() {
            val savedSlot = slot<PartnershipLead>()
            every { partnershipLeadAdaptor.existsByPhone(any()) } returns false
            every { partnershipLeadAdaptor.save(capture(savedSlot)) } answers { savedSlot.captured }

            useCase.execute(request(email = null, message = null))

            assertAll(
                { assertEquals(null, savedSlot.captured.email) },
                { assertEquals(null, savedSlot.captured.message) },
            )
        }
    }

    @Nested
    inner class Failure {
        @Test
        fun `이미 접수된 번호면 PartnershipLeadAlreadyExistsException을 던진다`() {
            every { partnershipLeadAdaptor.existsByPhone("01012345678") } returns true

            assertThrows<PartnershipLeadAlreadyExistsException> {
                useCase.execute(request(phone = "010-1234-5678"))
            }
            verify(exactly = 0) { partnershipLeadAdaptor.save(any()) }
        }
    }
}
