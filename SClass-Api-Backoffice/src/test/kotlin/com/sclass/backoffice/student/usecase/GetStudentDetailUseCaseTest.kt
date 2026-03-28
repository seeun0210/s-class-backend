package com.sclass.backoffice.student.usecase

import com.sclass.domain.domains.organization.adaptor.OrganizationAttributionAdaptor
import com.sclass.domain.domains.organization.domain.AttributionSource
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.domain.OrganizationAttribution
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.exception.StudentNotFoundException
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetStudentDetailUseCaseTest {
    private lateinit var studentAdaptor: StudentAdaptor
    private lateinit var userRoleAdaptor: UserRoleAdaptor
    private lateinit var organizationAttributionAdaptor: OrganizationAttributionAdaptor
    private lateinit var useCase: GetStudentDetailUseCase

    @BeforeEach
    fun setUp() {
        studentAdaptor = mockk()
        userRoleAdaptor = mockk()
        organizationAttributionAdaptor = mockk()
        useCase = GetStudentDetailUseCase(studentAdaptor, userRoleAdaptor, organizationAttributionAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `학생 상세 정보를 조회하여 응답을 반환한다`() {
            val user =
                User(
                    email = "student@example.com",
                    name = "김학생",
                    authProvider = AuthProvider.EMAIL,
                    phoneNumber = "010-1234-5678",
                )
            val student = Student(user = user)
            val roles =
                listOf(
                    UserRole(
                        userId = user.id,
                        platform = Platform.SUPPORTERS,
                        role = Role.STUDENT,
                        state = UserRoleState.NORMAL,
                    ),
                )
            val organization =
                Organization(id = 1L, name = "테스트 학원", domain = "test.sclass.com", logoUrl = "https://example.com/logo.png")
            val organizations = listOf(OrganizationUser(user = user, organization = organization))
            val attribution =
                OrganizationAttribution(
                    organizationId = 1L,
                    studentId = student.id,
                    source = AttributionSource.INVITE_CODE,
                )

            every { studentAdaptor.findByUserIdWithUser(user.id) } returns student
            every { userRoleAdaptor.findAllByUserId(user.id) } returns roles
            every { studentAdaptor.findDocumentsWithFileByStudentId(student.id) } returns emptyList()
            every { studentAdaptor.findOrganizationsByUserId(user.id) } returns organizations
            every { organizationAttributionAdaptor.findByStudentIdOrNull(student.id) } returns attribution

            val response = useCase.execute(user.id)

            assertThat(response.id).isEqualTo(student.id)
            assertThat(response.name).isEqualTo("김학생")
            assertThat(response.email).isEqualTo("student@example.com")
            assertThat(response.phoneNumber).isEqualTo("010-1234-5678")
            assertThat(response.roles).hasSize(1)
            assertThat(response.roles[0].platform).isEqualTo(Platform.SUPPORTERS)
            assertThat(response.roles[0].state).isEqualTo(UserRoleState.NORMAL)
            assertThat(response.documents).isEmpty()
            assertThat(response.organizations).hasSize(1)
            assertThat(response.organizations[0].id).isEqualTo(1L)
            assertThat(response.organizations[0].name).isEqualTo("테스트 학원")
            assertThat(response.organizations[0].attribution).isNotNull
            assertThat(response.organizations[0].attribution!!.source).isEqualTo(AttributionSource.INVITE_CODE)
        }

        @Test
        fun `연관 데이터가 없으면 빈 리스트로 응답한다`() {
            val user =
                User(
                    email = "new@example.com",
                    name = "신입학생",
                    authProvider = AuthProvider.EMAIL,
                )
            val student = Student(user = user)

            every { studentAdaptor.findByUserIdWithUser(user.id) } returns student
            every { userRoleAdaptor.findAllByUserId(user.id) } returns emptyList()
            every { studentAdaptor.findDocumentsWithFileByStudentId(student.id) } returns emptyList()
            every { studentAdaptor.findOrganizationsByUserId(user.id) } returns emptyList()
            every { organizationAttributionAdaptor.findByStudentIdOrNull(student.id) } returns null

            val response = useCase.execute(user.id)

            assertThat(response.id).isEqualTo(student.id)
            assertThat(response.roles).isEmpty()
            assertThat(response.documents).isEmpty()
            assertThat(response.organizations).isEmpty()
        }
    }

    @Nested
    inner class StudentNotFound {
        @Test
        fun `존재하지 않는 userId이면 StudentNotFoundException이 발생한다`() {
            every { studentAdaptor.findByUserIdWithUser("unknown-id") } throws StudentNotFoundException()

            assertThatThrownBy { useCase.execute("unknown-id") }
                .isInstanceOf(StudentNotFoundException::class.java)
        }
    }
}
