package com.sclass.backoffice.teacher.usecase

import com.sclass.domain.domains.file.domain.File
import com.sclass.domain.domains.file.domain.FileType
import com.sclass.domain.domains.organization.domain.Organization
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherDocument
import com.sclass.domain.domains.teacher.domain.TeacherDocumentType
import com.sclass.domain.domains.teacher.exception.TeacherNotFoundException
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

class GetTeacherDetailUseCaseTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var userRoleAdaptor: UserRoleAdaptor
    private lateinit var useCase: GetTeacherDetailUseCase

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        userRoleAdaptor = mockk()
        useCase = GetTeacherDetailUseCase(teacherAdaptor, userRoleAdaptor)
    }

    @Nested
    inner class Success {
        @Test
        fun `선생님 상세 정보를 조회하여 응답을 반환한다`() {
            val user =
                User(
                    email = "teacher@example.com",
                    name = "홍길동",
                    authProvider = AuthProvider.EMAIL,
                    phoneNumber = "010-1234-5678",
                    profileImageUrl = "https://example.com/profile.jpg",
                )
            val teacher = Teacher(user = user)
            val roles =
                listOf(
                    UserRole(
                        userId = user.id,
                        platform = Platform.SUPPORTERS,
                        role = Role.TEACHER,
                        state = UserRoleState.APPROVED,
                    ),
                )
            val file =
                File.create(
                    originalFilename = "certificate.pdf",
                    storedFilename = "stored.pdf",
                    mimeType = "application/pdf",
                    fileSize = 1024L,
                    fileType = FileType.TEACHER_CERTIFICATE,
                    uploadedBy = user.id,
                )
            val documents =
                listOf(
                    TeacherDocument(
                        teacher = teacher,
                        file = file,
                        documentType = TeacherDocumentType.COMPLETION_CERTIFICATE,
                    ),
                )
            val organization = Organization(id = 1L, name = "테스트 학원", domain = "test.sclass.com", logoUrl = "https://example.com/logo.png")
            val organizations = listOf(OrganizationUser(user = user, organization = organization))

            every { teacherAdaptor.findByUserIdWithUser(user.id) } returns teacher
            every { userRoleAdaptor.findAllByUserId(user.id) } returns roles
            every { teacherAdaptor.findDocumentsWithFileByTeacherId(teacher.id) } returns documents
            every { teacherAdaptor.findOrganizationsByUserId(user.id) } returns organizations

            val response = useCase.execute(user.id)

            assertThat(response.id).isEqualTo(teacher.id)
            assertThat(response.user.name).isEqualTo("홍길동")
            assertThat(response.user.email).isEqualTo("teacher@example.com")
            assertThat(response.user.phoneNumber).isEqualTo("010-1234-5678")
            assertThat(response.user.profileImageUrl).isEqualTo("https://example.com/profile.jpg")
            assertThat(response.roles).hasSize(1)
            assertThat(response.roles[0].platform).isEqualTo(Platform.SUPPORTERS)
            assertThat(response.roles[0].role).isEqualTo(Role.TEACHER)
            assertThat(response.roles[0].state).isEqualTo(UserRoleState.APPROVED)
            assertThat(response.documents).hasSize(1)
            assertThat(response.documents[0].documentType).isEqualTo(TeacherDocumentType.COMPLETION_CERTIFICATE)
            assertThat(response.documents[0].file.originalFilename).isEqualTo("certificate.pdf")
            assertThat(response.organizations).hasSize(1)
            assertThat(response.organizations[0].id).isEqualTo(1L)
            assertThat(response.organizations[0].name).isEqualTo("테스트 학원")
            assertThat(response.organizations[0].domain).isEqualTo("test.sclass.com")
            assertThat(response.organizations[0].logoUrl).isEqualTo("https://example.com/logo.png")
        }

        @Test
        fun `연관 데이터가 없으면 빈 리스트로 응답한다`() {
            val user =
                User(
                    email = "new@example.com",
                    name = "신입선생",
                    authProvider = AuthProvider.EMAIL,
                )
            val teacher = Teacher(user = user)

            every { teacherAdaptor.findByUserIdWithUser(user.id) } returns teacher
            every { userRoleAdaptor.findAllByUserId(user.id) } returns emptyList()
            every { teacherAdaptor.findDocumentsWithFileByTeacherId(teacher.id) } returns emptyList()
            every { teacherAdaptor.findOrganizationsByUserId(user.id) } returns emptyList()

            val response = useCase.execute(user.id)

            assertThat(response.id).isEqualTo(teacher.id)
            assertThat(response.roles).isEmpty()
            assertThat(response.documents).isEmpty()
            assertThat(response.organizations).isEmpty()
        }
    }

    @Nested
    inner class TeacherNotFound {
        @Test
        fun `존재하지 않는 userId이면 TeacherNotFoundException이 발생한다`() {
            every { teacherAdaptor.findByUserIdWithUser("unknown-id") } throws TeacherNotFoundException()

            assertThatThrownBy { useCase.execute("unknown-id") }
                .isInstanceOf(TeacherNotFoundException::class.java)
        }
    }
}
