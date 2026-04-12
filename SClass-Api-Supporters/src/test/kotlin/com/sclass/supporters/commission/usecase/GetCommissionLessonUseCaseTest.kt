package com.sclass.supporters.commission.usecase

import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.domain.domains.lesson.exception.LessonNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetCommissionLessonUseCaseTest {
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: GetCommissionLessonUseCase

    private val commissionId = 1L
    private val teacherUserId = "teacher-user-id-00000000001"
    private val studentUserId = "student-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        lessonAdaptor = mockk()
        useCase = GetCommissionLessonUseCase(lessonAdaptor)
    }

    private fun lesson() =
        Lesson(
            lessonType = LessonType.COMMISSION,
            sourceCommissionId = commissionId,
            studentUserId = studentUserId,
            assignedTeacherUserId = teacherUserId,
            name = "1회성 수학 의뢰",
            teacherPayoutAmountWon = 80_000,
        )

    @Test
    fun `의뢰에 연결된 레슨을 반환한다`() {
        every { lessonAdaptor.findByCommission(commissionId) } returns lesson()

        val result = useCase.execute(commissionId)

        assertAll(
            { assertEquals(LessonType.COMMISSION, result.lessonType) },
            { assertEquals(commissionId, result.sourceCommissionId) },
        )
    }

    @Test
    fun `의뢰에 연결된 레슨이 없으면 예외가 발생한다`() {
        every { lessonAdaptor.findByCommission(commissionId) } returns null

        assertThrows<LessonNotFoundException> {
            useCase.execute(commissionId)
        }
    }
}
