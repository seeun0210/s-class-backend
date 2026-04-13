package com.sclass.backoffice.lesson.usecase

import com.sclass.backoffice.lesson.dto.UpdateSubstituteTeacherRequest
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonSubstituteAssignNotAllowedException
import com.sclass.domain.domains.user.adaptor.UserAdaptor
import com.sclass.domain.domains.user.exception.UserNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateSubstituteTeacherUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var userAdaptor: UserAdaptor
    private lateinit var useCase: UpdateSubstituteTeacherUseCase

    private val assignedTeacher = "assigned-teacher-id-0000001"
    private val substitute = "substitute-teacher-id-00001"

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        userAdaptor = mockk()
        useCase = UpdateSubstituteTeacherUseCase(lessonAdaptor, userAdaptor)
    }

    private fun newLesson(
        status: LessonStatus = LessonStatus.SCHEDULED,
        substituteTeacherUserId: String? = null,
    ) = Lesson(
        id = 1L,
        lessonType = LessonType.COURSE,
        studentUserId = "student-user-id-0000000001",
        assignedTeacherUserId = assignedTeacher,
        substituteTeacherUserId = substituteTeacherUserId,
        name = "lesson",
        status = status,
    )

    @Test
    fun `teacherUserId가 있으면 대타 배정`() {
        val lesson = newLesson()
        every { userAdaptor.findById(substitute) } returns mockk()
        every { lessonAdaptor.findById(1L) } returns lesson

        val result = useCase.execute(1L, UpdateSubstituteTeacherRequest(substitute))

        assertEquals(substitute, result.substituteTeacherUserId)
    }

    @Test
    fun `teacherUserId가 null이면 대타 해제`() {
        val lesson = newLesson(substituteTeacherUserId = substitute)
        every { lessonAdaptor.findById(1L) } returns lesson

        val result = useCase.execute(1L, UpdateSubstituteTeacherRequest(null))

        assertNull(result.substituteTeacherUserId)
    }

    @Test
    fun `존재하지 않는 유저면 예외`() {
        every { lessonAdaptor.findById(1L) } returns newLesson()
        every { userAdaptor.findById(substitute) } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            useCase.execute(1L, UpdateSubstituteTeacherRequest(substitute))
        }
    }

    @Test
    fun `SCHEDULED가 아닐 때 대타 배정 예외`() {
        val lesson = newLesson(status = LessonStatus.IN_PROGRESS)
        every { userAdaptor.findById(substitute) } returns mockk()
        every { lessonAdaptor.findById(1L) } returns lesson

        assertThrows<LessonSubstituteAssignNotAllowedException> {
            useCase.execute(1L, UpdateSubstituteTeacherRequest(substitute))
        }
    }
}
