package com.sclass.supporters.course.usecase

import com.sclass.common.exception.ForbiddenException
import com.sclass.domain.domains.course.adaptor.CourseAdaptor
import com.sclass.domain.domains.course.domain.Course
import com.sclass.domain.domains.course.domain.CourseStatus
import com.sclass.domain.domains.course.dto.CourseWithEnrollmentCountDto
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.UserRole
import com.sclass.domain.domains.user.domain.UserRoleState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetMyCourseListUseCaseTest {
    private lateinit var courseAdaptor: CourseAdaptor
    private lateinit var userRoleAdaptor: UserRoleAdaptor
    private lateinit var useCase: GetMyCourseListUseCase

    @BeforeEach
    fun setUp() {
        courseAdaptor = mockk()
        userRoleAdaptor = mockk()
        useCase = GetMyCourseListUseCase(courseAdaptor, userRoleAdaptor)
    }

    private fun createUserRole(
        platform: Platform,
        role: Role = Role.TEACHER,
        state: UserRoleState = UserRoleState.NORMAL,
    ) = UserRole(
        userId = USER_ID,
        platform = platform,
        role = role,
        state = state,
    )

    private fun createCourseWithCount(
        id: Long,
        enrollmentCount: Long,
    ) = CourseWithEnrollmentCountDto(
        course =
            Course(
                id = id,
                productId = "product-id-0000000000000001",
                teacherUserId = USER_ID,
                name = "코스 $id",
                status = CourseStatus.ACTIVE,
            ),
        enrollmentCount = enrollmentCount,
    )

    @Test
    fun `LMS TEACHER는 내 코스 목록을 조회한다`() {
        val userRoles = listOf(createUserRole(Platform.LMS))
        val courses = listOf(createCourseWithCount(1L, 5), createCourseWithCount(2L, 3))

        every { userRoleAdaptor.findAllByUserIdAndRole(USER_ID, Role.TEACHER) } returns userRoles
        every { courseAdaptor.findAllByTeacherUserIdWithEnrollmentCount(USER_ID) } returns courses

        val result = useCase.execute(USER_ID, "TEACHER")

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(5L, result[0].enrollmentCount)
        assertEquals(2L, result[1].id)
        assertEquals(3L, result[1].enrollmentCount)
        verify { courseAdaptor.findAllByTeacherUserIdWithEnrollmentCount(USER_ID) }
    }

    @Test
    fun `Supporters TEACHER는 빈 배열을 반환한다`() {
        val userRoles = listOf(createUserRole(Platform.SUPPORTERS))

        every { userRoleAdaptor.findAllByUserIdAndRole(USER_ID, Role.TEACHER) } returns userRoles

        val result = useCase.execute(USER_ID, "TEACHER")

        assertTrue(result.isEmpty())
        verify(exactly = 0) { courseAdaptor.findAllByTeacherUserIdWithEnrollmentCount(any()) }
    }

    @Test
    fun `LMS TEACHER이지만 비활성 상태면 빈 배열을 반환한다`() {
        val userRoles = listOf(createUserRole(Platform.LMS, state = UserRoleState.PENDING))

        every { userRoleAdaptor.findAllByUserIdAndRole(USER_ID, Role.TEACHER) } returns userRoles

        val result = useCase.execute(USER_ID, "TEACHER")

        assertTrue(result.isEmpty())
        verify(exactly = 0) { courseAdaptor.findAllByTeacherUserIdWithEnrollmentCount(any()) }
    }

    @Test
    fun `STUDENT 역할이면 ForbiddenException을 던진다`() {
        assertThrows<ForbiddenException> {
            useCase.execute(USER_ID, "STUDENT")
        }
    }

    @Test
    fun `ADMIN 역할이면 ForbiddenException을 던진다`() {
        assertThrows<ForbiddenException> {
            useCase.execute(USER_ID, "ADMIN")
        }
    }

    @Test
    fun `코스가 없으면 빈 배열을 반환한다`() {
        val userRoles = listOf(createUserRole(Platform.LMS))

        every { userRoleAdaptor.findAllByUserIdAndRole(USER_ID, Role.TEACHER) } returns userRoles
        every { courseAdaptor.findAllByTeacherUserIdWithEnrollmentCount(USER_ID) } returns emptyList()

        val result = useCase.execute(USER_ID, "TEACHER")

        assertTrue(result.isEmpty())
    }

    companion object {
        private const val USER_ID = "01HXXXXXXXXXXXXXXXXXTCHRID"
    }
}
