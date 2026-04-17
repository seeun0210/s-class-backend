package com.sclass.backoffice.partnership.usecase

import com.sclass.domain.domains.partnership.adaptor.PartnershipLeadAdaptor
import com.sclass.domain.domains.partnership.domain.PartnershipLead
import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class GetPartnershipLeadsUseCaseTest {
    private lateinit var partnershipLeadAdaptor: PartnershipLeadAdaptor
    private lateinit var useCase: GetPartnershipLeadsUseCase

    @BeforeEach
    fun setUp() {
        partnershipLeadAdaptor = mockk()
        useCase = GetPartnershipLeadsUseCase(partnershipLeadAdaptor)
    }

    private fun lead(
        id: Long = 1L,
        status: PartnershipLeadStatus = PartnershipLeadStatus.NEW,
    ) = PartnershipLead(
        id = id,
        academyName = "서울학원",
        phone = "01012345678",
        email = null,
        message = null,
        status = status,
    )

    @Nested
    inner class Execute {
        @Test
        fun `status가 null이면 전체 조회한다`() {
            val page: Page<PartnershipLead> = PageImpl(listOf(lead(1L), lead(2L)))
            every { partnershipLeadAdaptor.findAll(Pageable.unpaged()) } returns page

            val result = useCase.execute(null, Pageable.unpaged())

            assertEquals(2, result.content.size)
            verify(exactly = 1) { partnershipLeadAdaptor.findAll(any()) }
            verify(exactly = 0) { partnershipLeadAdaptor.findAllByStatus(any(), any()) }
        }

        @Test
        fun `status가 주어지면 해당 상태로 필터해서 조회한다`() {
            val page: Page<PartnershipLead> = PageImpl(listOf(lead(1L, PartnershipLeadStatus.CONTACTED)))
            every {
                partnershipLeadAdaptor.findAllByStatus(PartnershipLeadStatus.CONTACTED, Pageable.unpaged())
            } returns page

            val result = useCase.execute(PartnershipLeadStatus.CONTACTED, Pageable.unpaged())

            assertEquals(1, result.content.size)
            assertEquals(PartnershipLeadStatus.CONTACTED, result.content.first().status)
            verify(exactly = 0) { partnershipLeadAdaptor.findAll(any()) }
            verify(exactly = 1) { partnershipLeadAdaptor.findAllByStatus(PartnershipLeadStatus.CONTACTED, any()) }
        }

        @Test
        fun `엔티티를 PartnershipLeadDetailResponse로 변환한다`() {
            val entity = lead(42L)
            every { partnershipLeadAdaptor.findAll(any()) } returns PageImpl(listOf(entity))

            val result = useCase.execute(null, Pageable.unpaged())

            val dto = result.content.first()
            assertEquals(42L, dto.id)
            assertEquals("서울학원", dto.academyName)
            assertEquals("01012345678", dto.phone)
        }
    }
}
