package com.sclass.domain.domains.organization.adaptor

import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.organization.dto.OrganizationUserInfo
import com.sclass.domain.domains.organization.exception.OrganizationUserNotFoundException
import com.sclass.domain.domains.organization.repository.OrganizationUserRepository
import com.sclass.domain.domains.user.domain.Role
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.Optional

class OrganizationUserAdaptorTest {
    private lateinit var organizationUserRepository: OrganizationUserRepository
    private lateinit var organizationUserAdaptor: OrganizationUserAdaptor

    @BeforeEach
    fun setUp() {
        organizationUserRepository = mockk()
        organizationUserAdaptor = OrganizationUserAdaptor(organizationUserRepository)
    }

    @Nested
    inner class FindById {
        @Test
        fun `존재하는 id로 조회하면 OrganizationUser를 반환한다`() {
            val orgUser = mockk<OrganizationUser>()
            every { organizationUserRepository.findById("org-user-id") } returns Optional.of(orgUser)

            val result = organizationUserAdaptor.findById("org-user-id")

            assertEquals(orgUser, result)
        }

        @Test
        fun `존재하지 않는 id로 조회하면 OrganizationUserNotFoundException이 발생한다`() {
            every { organizationUserRepository.findById("unknown-id") } returns Optional.empty()

            assertThrows<OrganizationUserNotFoundException> {
                organizationUserAdaptor.findById("unknown-id")
            }
        }
    }

    @Nested
    inner class FindByUserIdAndOrganizationId {
        @Test
        fun `존재하면 OrganizationUser를 반환한다`() {
            val orgUser = mockk<OrganizationUser>()
            every { organizationUserRepository.findByUserIdAndOrganizationId("user-id", 1L) } returns orgUser

            val result = organizationUserAdaptor.findByUserIdAndOrganizationId("user-id", 1L)

            assertEquals(orgUser, result)
        }

        @Test
        fun `존재하지 않으면 OrganizationUserNotFoundException이 발생한다`() {
            every { organizationUserRepository.findByUserIdAndOrganizationId("user-id", 1L) } returns null

            assertThrows<OrganizationUserNotFoundException> {
                organizationUserAdaptor.findByUserIdAndOrganizationId("user-id", 1L)
            }
        }
    }

    @Nested
    inner class FindByUserIdAndOrganizationIdOrNull {
        @Test
        fun `존재하면 OrganizationUser를 반환한다`() {
            val orgUser = mockk<OrganizationUser>()
            every { organizationUserRepository.findByUserIdAndOrganizationId("user-id", 1L) } returns orgUser

            val result = organizationUserAdaptor.findByUserIdAndOrganizationIdOrNull("user-id", 1L)

            assertEquals(orgUser, result)
        }

        @Test
        fun `존재하지 않으면 null을 반환한다`() {
            every { organizationUserRepository.findByUserIdAndOrganizationId("user-id", 1L) } returns null

            val result = organizationUserAdaptor.findByUserIdAndOrganizationIdOrNull("user-id", 1L)

            assertNull(result)
        }
    }

    @Nested
    inner class FindAllByOrganizationId {
        @Test
        fun `기관의 소속 유저 목록을 반환한다`() {
            val orgUsers = listOf(mockk<OrganizationUser>(), mockk<OrganizationUser>())
            every { organizationUserRepository.findAllByOrganizationId(1L) } returns orgUsers

            val result = organizationUserAdaptor.findAllByOrganizationId(1L)

            assertEquals(2, result.size)
            assertEquals(orgUsers, result)
        }

        @Test
        fun `소속 유저가 없으면 빈 리스트를 반환한다`() {
            every { organizationUserRepository.findAllByOrganizationId(1L) } returns emptyList()

            val result = organizationUserAdaptor.findAllByOrganizationId(1L)

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class FindAllByUserId {
        @Test
        fun `유저의 소속 기관 목록을 반환한다`() {
            val orgUsers = listOf(mockk<OrganizationUser>(), mockk<OrganizationUser>())
            every { organizationUserRepository.findAllByUserId("user-id") } returns orgUsers

            val result = organizationUserAdaptor.findAllByUserId("user-id")

            assertEquals(2, result.size)
            assertEquals(orgUsers, result)
        }

        @Test
        fun `소속 기관이 없으면 빈 리스트를 반환한다`() {
            every { organizationUserRepository.findAllByUserId("user-id") } returns emptyList()

            val result = organizationUserAdaptor.findAllByUserId("user-id")

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class ExistsByUserIdAndOrganizationId {
        @Test
        fun `소속되어 있으면 true를 반환한다`() {
            every { organizationUserRepository.existsByUserIdAndOrganizationId("user-id", 1L) } returns true

            val result = organizationUserAdaptor.existsByUserIdAndOrganizationId("user-id", 1L)

            assertTrue(result)
        }

        @Test
        fun `소속되어 있지 않으면 false를 반환한다`() {
            every { organizationUserRepository.existsByUserIdAndOrganizationId("user-id", 1L) } returns false

            val result = organizationUserAdaptor.existsByUserIdAndOrganizationId("user-id", 1L)

            assertFalse(result)
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `저장을 repository에 위임한다`() {
            val orgUser = mockk<OrganizationUser>()
            every { organizationUserRepository.save(orgUser) } returns orgUser

            val result = organizationUserAdaptor.save(orgUser)

            assertEquals(orgUser, result)
            verify { organizationUserRepository.save(orgUser) }
        }
    }

    @Nested
    inner class FindUsersByOrganizationIdAndRole {
        @Test
        fun `organizationId와 role로 유저 목록을 조회하면 페이지를 반환한다`() {
            val pageable = PageRequest.of(0, 20)
            val userInfo =
                OrganizationUserInfo(
                    userId = "user-id",
                    name = "테스트",
                    email = "test@example.com",
                    profileImageUrl = null,
                    role = Role.TEACHER,
                    createdAt = LocalDateTime.now(),
                )
            val page = PageImpl(listOf(userInfo), pageable, 1L)

            every {
                organizationUserRepository.findUsersByOrganizationIdAndRole(1L, Role.TEACHER, pageable)
            } returns page

            val result = organizationUserAdaptor.findUsersByOrganizationIdAndRole(1L, Role.TEACHER, pageable)

            assertEquals(1, result.totalElements)
            assertEquals("user-id", result.content[0].userId)
            verify { organizationUserRepository.findUsersByOrganizationIdAndRole(1L, Role.TEACHER, pageable) }
        }

        @Test
        fun `해당 역할의 유저가 없으면 빈 페이지를 반환한다`() {
            val pageable = PageRequest.of(0, 20)
            val emptyPage = PageImpl<OrganizationUserInfo>(emptyList(), pageable, 0L)

            every {
                organizationUserRepository.findUsersByOrganizationIdAndRole(1L, Role.ADMIN, pageable)
            } returns emptyPage

            val result = organizationUserAdaptor.findUsersByOrganizationIdAndRole(1L, Role.ADMIN, pageable)

            assertEquals(0, result.totalElements)
        }
    }

    @Nested
    inner class CountByOrganizationIdGroupByRole {
        @Test
        fun `organizationId로 역할별 카운트를 조회하면 Map을 반환한다`() {
            val roleCounts = mapOf(Role.ADMIN to 2L, Role.TEACHER to 5L, Role.STUDENT to 30L)

            every {
                organizationUserRepository.countByOrganizationIdGroupByRole(1L)
            } returns roleCounts

            val result = organizationUserAdaptor.countByOrganizationIdGroupByRole(1L)

            assertEquals(2L, result[Role.ADMIN])
            assertEquals(5L, result[Role.TEACHER])
            assertEquals(30L, result[Role.STUDENT])
            verify { organizationUserRepository.countByOrganizationIdGroupByRole(1L) }
        }

        @Test
        fun `소속 유저가 없으면 빈 Map을 반환한다`() {
            every {
                organizationUserRepository.countByOrganizationIdGroupByRole(1L)
            } returns emptyMap()

            val result = organizationUserAdaptor.countByOrganizationIdGroupByRole(1L)

            assertEquals(0, result.size)
        }
    }
}
