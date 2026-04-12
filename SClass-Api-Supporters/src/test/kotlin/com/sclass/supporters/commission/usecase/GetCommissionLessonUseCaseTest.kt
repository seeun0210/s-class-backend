package com.sclass.supporters.commission.usecase

import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.exception.CommissionUnauthorizedAccessException
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
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var useCase: GetCommissionLessonUseCase

    private val commissionId = 1L
    private val teacherUserId = "teacher-user-id-00000000001"
    private val studentUserId = "student-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        lessonAdaptor = mockk()
        useCase = GetCommissionLessonUseCase(commissionAdaptor, lessonAdaptor)
    }

    private fun commission() =
        mockk<Commission>(relaxed = true) {
            every { studentUserId } returns this@GetCommissionLessonUseCaseTest.studentUserId
            every { teacherUserId } returns this@GetCommissionLessonUseCaseTest.teacherUserId
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
    fun `학생이 자신의 의뢰 레슨을 조회한다`() {
        every { commissionAdaptor.findById(commissionId) } returns commission()
        every { lessonAdaptor.findByCommission(commissionId) } returns lesson()

        val result = useCase.execute(studentUserId, commissionId)

        assertAll(
            { assertEquals(LessonType.COMMISSION, result.lessonType) },
            { assertEquals(commissionId, result.sourceCommissionId) },
        )
    }

    @Test
    fun `선생님이 담당 의뢰 레슨을 조회한다`() {
        every { commissionAdaptor.findById(commissionId) } returns commission()
        every { lessonAdaptor.findByCommission(commissionId) } returns lesson()

        val result = useCase.execute(teacherUserId, commissionId)

        assertAll(
            { assertEquals(LessonType.COMMISSION, result.lessonType) },
            { assertEquals(commissionId, result.sourceCommissionId) },
        )
    }

    @Test
    fun `당사자가 아니면 CommissionUnauthorizedAccessException이 발생한다`() {
        every { commissionAdaptor.findById(commissionId) } returns commission()

        assertThrows<CommissionUnauthorizedAccessException> {
            useCase.execute("other-user-id-0000000000001", commissionId)
        }
    }

    @Test
    fun `의뢰에 연결된 레슨이 없으면 LessonNotFoundException이 발생한다`() {
        every { commissionAdaptor.findById(commissionId) } returns commission()
        every { lessonAdaptor.findByCommission(commissionId) } returns null

        assertThrows<LessonNotFoundException> {
            useCase.execute(studentUserId, commissionId)
        }
    }
}
