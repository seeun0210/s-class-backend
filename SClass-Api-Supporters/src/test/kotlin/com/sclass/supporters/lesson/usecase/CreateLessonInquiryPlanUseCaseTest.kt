package com.sclass.supporters.lesson.usecase

import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.supporters.inquiry.dto.CreateInquiryPlanRequest
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import com.sclass.supporters.inquiry.usecase.CreateInquiryPlanUseCase
import com.sclass.supporters.lesson.dto.CreateLessonInquiryPlanRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateLessonInquiryPlanUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var createInquiryPlanUseCase: CreateInquiryPlanUseCase
    private lateinit var useCase: CreateLessonInquiryPlanUseCase

    private val teacherUserId = "teacher-user-id-00000000001"
    private val studentUserId = "student-user-id-0000000001"

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        createInquiryPlanUseCase = mockk()
        useCase = CreateLessonInquiryPlanUseCase(lessonAdaptor, createInquiryPlanUseCase)
    }

    private fun lesson(id: Long = 1L) =
        Lesson(
            id = id,
            lessonType = LessonType.COURSE,
            enrollmentId = 1L,
            studentUserId = studentUserId,
            assignedTeacherUserId = teacherUserId,
            lessonNumber = 1,
            name = "수학 1회차",
            teacherPayoutAmountWon = 50_000,
        )

    @Test
    fun `선생님이 담당 레슨의 탐구 계획을 생성한다`() {
        val lesson = lesson()
        val requestSlot = slot<CreateInquiryPlanRequest>()
        val mockResponse = mockk<InquiryPlanResponse>()

        every { lessonAdaptor.findById(1L) } returns lesson
        every { createInquiryPlanUseCase.execute(teacherUserId, capture(requestSlot)) } returns mockResponse

        useCase.execute(teacherUserId, 1L, CreateLessonInquiryPlanRequest(paragraph = "탐구 내용"))

        assertAll(
            { assertEquals(InquiryPlanSourceType.LESSON, requestSlot.captured.sourceType) },
            { assertEquals(1L, requestSlot.captured.sourceRefId) },
            { assertEquals("탐구 내용", requestSlot.captured.paragraph) },
        )
        verify(exactly = 1) { createInquiryPlanUseCase.execute(any(), any()) }
    }

    @Test
    fun `학생은 탐구 계획을 생성할 수 없다`() {
        every { lessonAdaptor.findById(1L) } returns lesson()

        assertThrows<IllegalArgumentException> {
            useCase.execute(studentUserId, 1L, CreateLessonInquiryPlanRequest(paragraph = "탐구 내용"))
        }
    }

    @Test
    fun `담당 선생님이 아니면 탐구 계획을 생성할 수 없다`() {
        every { lessonAdaptor.findById(1L) } returns lesson()

        assertThrows<IllegalArgumentException> {
            useCase.execute("other-teacher-id-000000001", 1L, CreateLessonInquiryPlanRequest(paragraph = "탐구 내용"))
        }
    }
}
