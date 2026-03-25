package com.sclass.domain.domains.organization.adaptor

import com.sclass.domain.domains.organization.domain.OrganizationAttribution
import com.sclass.domain.domains.organization.exception.OrganizationAttributionNotFoundException
import com.sclass.domain.domains.organization.repository.OrganizationAttributionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class OrganizationAttributionAdaptorTest {
    private lateinit var organizationAttributionRepository: OrganizationAttributionRepository
    private lateinit var organizationAttributionAdaptor: OrganizationAttributionAdaptor

    @BeforeEach
    fun setUp() {
        organizationAttributionRepository = mockk()
        organizationAttributionAdaptor = OrganizationAttributionAdaptor(organizationAttributionRepository)
    }

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 귀속 정보를 반환한다`() {
            val attribution = mockk<OrganizationAttribution>()
            every { organizationAttributionRepository.findById("attr-id") } returns Optional.of(attribution)

            val result = organizationAttributionAdaptor.findById("attr-id")

            assertEquals(attribution, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 OrganizationAttributionNotFoundException이 발생한다`() {
            every { organizationAttributionRepository.findById("unknown-id") } returns Optional.empty()

            assertThrows<OrganizationAttributionNotFoundException> {
                organizationAttributionAdaptor.findById("unknown-id")
            }
        }
    }

    @Nested
    inner class FindByStudentId {
        @Test
        fun `존재하는 studentId로 조회하면 귀속 정보를 반환한다`() {
            val attribution = mockk<OrganizationAttribution>()
            every { organizationAttributionRepository.findByStudentId("student-id") } returns attribution

            val result = organizationAttributionAdaptor.findByStudentId("student-id")

            assertEquals(attribution, result)
        }

        @Test
        fun `존재하지 않는 studentId로 조회하면 OrganizationAttributionNotFoundException이 발생한다`() {
            every { organizationAttributionRepository.findByStudentId("unknown-id") } returns null

            assertThrows<OrganizationAttributionNotFoundException> {
                organizationAttributionAdaptor.findByStudentId("unknown-id")
            }
        }
    }

    @Nested
    inner class FindByStudentIdOrNull {
        @Test
        fun `존재하는 studentId로 조회하면 귀속 정보를 반환한다`() {
            val attribution = mockk<OrganizationAttribution>()
            every { organizationAttributionRepository.findByStudentId("student-id") } returns attribution

            val result = organizationAttributionAdaptor.findByStudentIdOrNull("student-id")

            assertEquals(attribution, result)
        }

        @Test
        fun `존재하지 않는 studentId로 조회하면 null을 반환한다`() {
            every { organizationAttributionRepository.findByStudentId("unknown-id") } returns null

            val result = organizationAttributionAdaptor.findByStudentIdOrNull("unknown-id")

            assertNull(result)
        }
    }

    @Nested
    inner class FindAllByOrganizationId {
        @Test
        fun `기관 id로 조회하면 해당 기관의 귀속 목록을 반환한다`() {
            val attributions = listOf(mockk<OrganizationAttribution>(), mockk<OrganizationAttribution>())
            every { organizationAttributionRepository.findAllByOrganizationId(1L) } returns attributions

            val result = organizationAttributionAdaptor.findAllByOrganizationId(1L)

            assertEquals(2, result.size)
            assertEquals(attributions, result)
        }

        @Test
        fun `귀속 정보가 없으면 빈 리스트를 반환한다`() {
            every { organizationAttributionRepository.findAllByOrganizationId(1L) } returns emptyList()

            val result = organizationAttributionAdaptor.findAllByOrganizationId(1L)

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class ExistsByStudentId {
        @Test
        fun `귀속 정보가 존재하면 true를 반환한다`() {
            every { organizationAttributionRepository.existsByStudentId("student-id") } returns true

            val result = organizationAttributionAdaptor.existsByStudentId("student-id")

            assertTrue(result)
        }

        @Test
        fun `귀속 정보가 없으면 false를 반환한다`() {
            every { organizationAttributionRepository.existsByStudentId("unknown-id") } returns false

            val result = organizationAttributionAdaptor.existsByStudentId("unknown-id")

            assertFalse(result)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `귀속 정보 저장을 repository에 위임한다`() {
            val attribution = mockk<OrganizationAttribution>()
            every { organizationAttributionRepository.save(attribution) } returns attribution

            val result = organizationAttributionAdaptor.save(attribution)

            assertEquals(attribution, result)
            verify { organizationAttributionRepository.save(attribution) }
        }
    }
}
