package com.sclass.domain.domains.teacher.service

import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.Teacher
import com.sclass.domain.domains.teacher.domain.TeacherEducation
import com.sclass.domain.domains.teacher.exception.TeacherAlreadyExistsException
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.AuthProvider
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import com.sclass.domain.domains.user.exception.RoleNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class TeacherDomainServiceTest {
    private lateinit var teacherAdaptor: TeacherAdaptor
    private lateinit var teacherDocumentAdaptor: TeacherDocumentAdaptor
    private lateinit var userRoleAdaptor: UserRoleAdaptor
    private lateinit var teacherDomainService: TeacherDomainService

    private lateinit var user: User
    private lateinit var teacher: Teacher

    @BeforeEach
    fun setUp() {
        teacherAdaptor = mockk()
        teacherDocumentAdaptor = mockk()
        userRoleAdaptor = mockk()
        teacherDomainService = TeacherDomainService(teacherAdaptor, teacherDocumentAdaptor, userRoleAdaptor)
        user =
            User(
                email = "teacher@test.com",
                name = "테스트선생님",
                authProvider = AuthProvider.EMAIL,
            )
        teacher = Teacher(user = user)
    }

    @Nested
    inner class Register {
        @Test
        fun `교사를 등록한다`() {
            val slot = slot<Teacher>()
            every { teacherAdaptor.existsByUserId(user.id) } returns false
            every { teacherAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = teacherDomainService.register(user = user)

            assertEquals(user, result.user)
        }

        @Test
        fun `교육정보와 함께 교사를 등록한다`() {
            val education =
                TeacherEducation(
                    university = "서울대학교",
                    major = "컴퓨터공학",
                    majorCategory = MajorCategory.ENGINEERING,
                )
            val slot = slot<Teacher>()
            every { teacherAdaptor.existsByUserId(user.id) } returns false
            every { teacherAdaptor.save(capture(slot)) } answers { slot.captured }

            val result = teacherDomainService.register(user = user, education = education)

            assertEquals(user, result.user)
            assertEquals("서울대학교", result.education.university)
            assertEquals("컴퓨터공학", result.education.major)
            assertEquals(MajorCategory.ENGINEERING, result.education.majorCategory)
        }

        @Test
        fun `이미 등록된 교사이면 TeacherAlreadyExistsException이 발생한다`() {
            every { teacherAdaptor.existsByUserId(user.id) } returns true

            assertThrows<TeacherAlreadyExistsException> {
                teacherDomainService.register(user = user)
            }
        }
    }

    @Nested
    inner class UpdateProfile {
        @Test
        fun `프로필을 수정한다`() {
            val userRole =
                UserRole(
                    userId = user.id,
                    platform = Platform.SUPPORTERS,
                    role = Role.TEACHER,
                    state = UserRoleState.DRAFT,
                )
            val slot = slot<Teacher>()
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.TEACHER)
            } returns userRole
            every { teacherAdaptor.save(capture(slot)) } answers { slot.captured }

            val result =
                teacherDomainService.updateProfile(
                    teacher = teacher,
                    platform = Platform.SUPPORTERS,
                    birthDate = LocalDate.of(2000, 1, 1),
                    selfIntroduction = "안녕하세요",
                    majorCategory = MajorCategory.ENGINEERING,
                    university = "서울대학교",
                    major = "컴퓨터공학",
                    highSchool = "한영고등학교",
                    address = "서울시 강남구",
                    residentNumber = "000101-3000000",
                )

            assertEquals("서울대학교", result.education.university)
            assertEquals("컴퓨터공학", result.education.major)
            assertEquals(MajorCategory.ENGINEERING, result.education.majorCategory)
        }

        @Test
        fun `역할이 없으면 RoleNotFoundException이 발생한다`() {
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.TEACHER)
            } returns null

            assertThrows<RoleNotFoundException> {
                teacherDomainService.updateProfile(
                    teacher = teacher,
                    platform = Platform.SUPPORTERS,
                    birthDate = LocalDate.of(2000, 1, 1),
                    selfIntroduction = null,
                    majorCategory = MajorCategory.ENGINEERING,
                    university = "서울대학교",
                    major = "컴퓨터공학",
                    highSchool = "한영고등학교",
                    address = "서울시 강남구",
                    residentNumber = "000101-3000000",
                )
            }
        }
    }

    @Nested
    inner class Approve {
        @Test
        fun `교사를 승인한다`() {
            val userRole =
                UserRole(
                    userId = user.id,
                    platform = Platform.SUPPORTERS,
                    role = Role.TEACHER,
                    state = UserRoleState.PENDING,
                )
            val slot = slot<Teacher>()
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.TEACHER)
            } returns userRole
            every { userRoleAdaptor.save(any()) } returns userRole
            every { teacherAdaptor.save(capture(slot)) } answers { slot.captured }

            val result =
                teacherDomainService.approve(
                    teacher = teacher,
                    platform = Platform.SUPPORTERS,
                    approvedBy = "admin-id",
                )

            assertEquals("admin-id", result.verification.approvedBy)
            assertEquals(UserRoleState.APPROVED, userRole.state)
            verify { userRoleAdaptor.save(userRole) }
        }

        @Test
        fun `역할이 없으면 RoleNotFoundException이 발생한다`() {
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.TEACHER)
            } returns null

            assertThrows<RoleNotFoundException> {
                teacherDomainService.approve(
                    teacher = teacher,
                    platform = Platform.SUPPORTERS,
                    approvedBy = "admin-id",
                )
            }
        }
    }

    @Nested
    inner class Reject {
        @Test
        fun `교사를 반려한다`() {
            val userRole =
                UserRole(
                    userId = user.id,
                    platform = Platform.SUPPORTERS,
                    role = Role.TEACHER,
                    state = UserRoleState.PENDING,
                )
            val slot = slot<Teacher>()
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.TEACHER)
            } returns userRole
            every { userRoleAdaptor.save(any()) } returns userRole
            every { teacherAdaptor.save(capture(slot)) } answers { slot.captured }

            val result =
                teacherDomainService.reject(
                    teacher = teacher,
                    platform = Platform.SUPPORTERS,
                    reason = "서류 미비",
                )

            assertEquals("서류 미비", result.verification.rejectionReason)
            assertEquals(UserRoleState.REJECTED, userRole.state)
            verify { userRoleAdaptor.save(userRole) }
        }

        @Test
        fun `역할이 없으면 RoleNotFoundException이 발생한다`() {
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.TEACHER)
            } returns null

            assertThrows<RoleNotFoundException> {
                teacherDomainService.reject(
                    teacher = teacher,
                    platform = Platform.SUPPORTERS,
                    reason = "서류 미비",
                )
            }
        }
    }

    @Nested
    inner class SubmitForVerification {
        @Test
        fun `역할이 없으면 RoleNotFoundException이 발생한다`() {
            every {
                userRoleAdaptor.findByUserIdAndPlatformAndRole(user.id, Platform.SUPPORTERS, Role.TEACHER)
            } returns null

            assertThrows<RoleNotFoundException> {
                teacherDomainService.submitForVerification(
                    teacher = teacher,
                    platform = Platform.SUPPORTERS,
                )
            }
        }
    }
}
