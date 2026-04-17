package com.sclass.domain.domains.partnership.adaptor

import com.sclass.domain.domains.partnership.domain.PartnershipLead
import com.sclass.domain.domains.partnership.domain.PartnershipLeadStatus
import com.sclass.domain.domains.partnership.exception.PartnershipLeadNotFoundException
import com.sclass.domain.domains.partnership.repository.PartnershipLeadRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.Optional

class PartnershipLeadAdaptorTest {
    private lateinit var repository: PartnershipLeadRepository
    private lateinit var adaptor: PartnershipLeadAdaptor

    @BeforeEach
    fun setUp() {
        repository = mockk()
        adaptor = PartnershipLeadAdaptor(repository)
    }

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id는 엔티티를 반환한다`() {
            val lead = mockk<PartnershipLead>()
            every { repository.findById(1L) } returns Optional.of(lead)

            assertEquals(lead, adaptor.findById(1L))
        }

        @Test
        fun `존재하지 않는 id는 PartnershipLeadNotFoundException을 던진다`() {
            every { repository.findById(99L) } returns Optional.empty()

            assertThrows<PartnershipLeadNotFoundException> { adaptor.findById(99L) }
        }
    }

    @Nested
    inner class ExistsByPhone {
        @Test
        fun `저장된 번호면 true를 반환한다`() {
            every { repository.existsByPhone("01012345678") } returns true

            assertTrue(adaptor.existsByPhone("01012345678"))
        }

        @Test
        fun `저장되지 않은 번호면 false를 반환한다`() {
            every { repository.existsByPhone("01099999999") } returns false

            assertFalse(adaptor.existsByPhone("01099999999"))
        }
    }

    @Nested
    inner class FindAllByStatus {
        @Test
        fun `status로 필터한 Page를 반환한다`() {
            val leads = listOf(mockk<PartnershipLead>(), mockk())
            val page: Page<PartnershipLead> = PageImpl(leads)
            every { repository.findAllByStatus(PartnershipLeadStatus.NEW, Pageable.unpaged()) } returns page

            val result = adaptor.findAllByStatus(PartnershipLeadStatus.NEW, Pageable.unpaged())

            assertEquals(2, result.content.size)
        }
    }
}
