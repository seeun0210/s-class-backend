package com.sclass.supporters.lesson.usecase

import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonStatus
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonInvalidStatusTransitionException
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
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
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class CreateLessonInquiryPlanUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var createInquiryPlanUseCase: CreateInquiryPlanUseCase
    private lateinit var txTemplate: TransactionTemplate
    private lateinit var useCase: CreateLessonInquiryPlanUseCase

    private val teacherUserId = "teacher-user-id-00000000001"
    private val studentUserId = "student-user-id-0000000001"
    private val zoneId = ZoneId.of("Asia/Seoul")
    private val fixedNow = LocalDateTime.of(2026, 4, 26, 10, 30)
    private val clock = Clock.fixed(fixedNow.atZone(zoneId).toInstant(), zoneId)

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        createInquiryPlanUseCase = mockk()
        txTemplate = mockk()
        every { txTemplate.execute(any<TransactionCallback<Any?>>()) } answers {
            firstArg<TransactionCallback<Any?>>().doInTransaction(mockk())
        }
        useCase = CreateLessonInquiryPlanUseCase(lessonAdaptor, createInquiryPlanUseCase, txTemplate, clock)
    }

    private fun lesson(
        id: Long = 1L,
        lessonType: LessonType = LessonType.COURSE,
        status: LessonStatus = LessonStatus.SCHEDULED,
    ) = Lesson(
        id = id,
        lessonType = lessonType,
        enrollmentId = 1L,
        sourceCommissionId = if (lessonType == LessonType.COMMISSION) 1L else null,
        studentUserId = studentUserId,
        assignedTeacherUserId = teacherUserId,
        lessonNumber = 1,
        name = "수학 1회차",
        status = status,
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
        verify(exactly = 0) { lessonAdaptor.save(any()) }
    }

    @Test
    fun `commission lesson에서 계획을 생성하면 lesson을 진행 상태로 전환한다`() {
        val lesson = lesson(lessonType = LessonType.COMMISSION)
        val mockResponse = mockk<InquiryPlanResponse>()

        every { lessonAdaptor.findById(1L) } returns lesson
        every { lessonAdaptor.save(lesson) } returns lesson
        every { createInquiryPlanUseCase.execute(any(), any()) } returns mockResponse

        useCase.execute(teacherUserId, 1L, CreateLessonInquiryPlanRequest(paragraph = "탐구 내용"))

        assertAll(
            { assertEquals(LessonStatus.IN_PROGRESS, lesson.status) },
            { assertEquals(teacherUserId, lesson.actualTeacherUserId) },
            { assertEquals(fixedNow, lesson.startedAt) },
        )
        verify(exactly = 1) { lessonAdaptor.save(lesson) }
    }

    @Test
    fun `이미 진행 중인 commission lesson은 다시 시작하지 않고 계획을 생성한다`() {
        val lesson = lesson(lessonType = LessonType.COMMISSION, status = LessonStatus.IN_PROGRESS)
        val mockResponse = mockk<InquiryPlanResponse>()

        every { lessonAdaptor.findById(1L) } returns lesson
        every { createInquiryPlanUseCase.execute(any(), any()) } returns mockResponse

        useCase.execute(teacherUserId, 1L, CreateLessonInquiryPlanRequest(paragraph = "탐구 내용"))

        verify(exactly = 0) { lessonAdaptor.save(any()) }
        verify(exactly = 1) { createInquiryPlanUseCase.execute(any(), any()) }
    }

    @Test
    fun `완료된 commission lesson은 계획을 생성할 수 없다`() {
        every { lessonAdaptor.findById(1L) } returns
            lesson(lessonType = LessonType.COMMISSION, status = LessonStatus.COMPLETED)

        assertThrows<LessonInvalidStatusTransitionException> {
            useCase.execute(teacherUserId, 1L, CreateLessonInquiryPlanRequest(paragraph = "탐구 내용"))
        }
        verify(exactly = 0) { lessonAdaptor.save(any()) }
        verify(exactly = 0) { createInquiryPlanUseCase.execute(any(), any()) }
    }

    @Test
    fun `취소된 commission lesson은 계획을 생성할 수 없다`() {
        every { lessonAdaptor.findById(1L) } returns
            lesson(lessonType = LessonType.COMMISSION, status = LessonStatus.CANCELLED)

        assertThrows<LessonInvalidStatusTransitionException> {
            useCase.execute(teacherUserId, 1L, CreateLessonInquiryPlanRequest(paragraph = "탐구 내용"))
        }
        verify(exactly = 0) { lessonAdaptor.save(any()) }
        verify(exactly = 0) { createInquiryPlanUseCase.execute(any(), any()) }
    }

    @Test
    fun `학생은 탐구 계획을 생성할 수 없다`() {
        every { lessonAdaptor.findById(1L) } returns lesson()

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute(studentUserId, 1L, CreateLessonInquiryPlanRequest(paragraph = "탐구 내용"))
        }
    }

    @Test
    fun `담당 선생님이 아니면 탐구 계획을 생성할 수 없다`() {
        every { lessonAdaptor.findById(1L) } returns lesson()

        assertThrows<LessonUnauthorizedAccessException> {
            useCase.execute("other-teacher-id-000000001", 1L, CreateLessonInquiryPlanRequest(paragraph = "탐구 내용"))
        }
    }
}
