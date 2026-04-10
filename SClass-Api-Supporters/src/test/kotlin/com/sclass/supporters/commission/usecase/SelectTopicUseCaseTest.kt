package com.sclass.supporters.commission.usecase

import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionTopicAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.CommissionTopic
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.domain.Lesson
import com.sclass.domain.domains.lesson.domain.LessonType
import com.sclass.supporters.commission.dto.SelectTopicRequest
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SelectTopicUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var commissionTopicAdaptor: CommissionTopicAdaptor
    private lateinit var lessonAdaptor: LessonAdaptor
    private lateinit var commissionReminderScheduler: CommissionReminderScheduler
    private lateinit var useCase: SelectTopicUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        commissionTopicAdaptor = mockk()
        lessonAdaptor = mockk()
        commissionReminderScheduler = mockk(relaxed = true)
        useCase = SelectTopicUseCase(commissionAdaptor, commissionTopicAdaptor, lessonAdaptor, commissionReminderScheduler)
    }

    private fun createCommission(
        id: Long = 1L,
        status: CommissionStatus = CommissionStatus.TOPIC_PROPOSED,
    ) = Commission(
        id = id,
        studentUserId = studentUserId,
        teacherUserId = teacherUserId,
        productId = "product-id-00000000000001",
        teacherPayoutAmountWon = 50_000,
        outputFormat = OutputFormat.REPORT,
        activityType = ActivityType.CAREER_EXPLORATION,
        status = status,
        guideInfo =
            GuideInfo(
                subject = "미시경제학",
                volume = "A4 3매",
                gradingCriteria = "평가기준",
                teacherEmphasis = "강조사항",
            ),
    )

    private fun createTopic(
        id: Long = 10L,
        commission: Commission,
    ) = CommissionTopic(
        id = id,
        commission = commission,
        topicId = "mongo-id-001",
        title = "주제 1",
        description = "설명 1",
    )

    private fun savedLesson(commissionId: Long) =
        Lesson(
            id = 100L,
            lessonType = LessonType.COMMISSION,
            sourceCommissionId = commissionId,
            studentUserId = studentUserId,
            assignedTeacherUserId = teacherUserId,
            name = "미시경제학",
            teacherPayoutAmountWon = 50_000,
        )

    @Test
    fun `학생이 주제를 선택하면 Lesson이 생성되고 Commission이 ACCEPTED 상태가 된다`() {
        val commission = createCommission()
        val topic = createTopic(commission = commission)
        val lessonSlot = slot<Lesson>()

        every { commissionAdaptor.findById(1L) } returns commission
        every { commissionTopicAdaptor.findById(10L) } returns topic
        every { lessonAdaptor.save(capture(lessonSlot)) } returns savedLesson(1L)

        val result = useCase.execute(studentUserId, 1L, 10L, SelectTopicRequest(isSelected = true))

        assertAll(
            { assertEquals(10L, result.id) },
            { assertEquals("주제 1", result.title) },
            { assertTrue(result.selected) },
            { assertEquals(CommissionStatus.ACCEPTED, commission.status) },
            { assertEquals(10L, commission.selectedTopicId) },
            { assertEquals(100L, commission.acceptedLessonId) },
            { assertTrue(topic.selected) },
            { assertEquals(LessonType.COMMISSION, lessonSlot.captured.lessonType) },
            { assertEquals(1L, lessonSlot.captured.sourceCommissionId) },
        )
    }

    @Test
    fun `isSelected가 false이면 예외가 발생한다`() {
        assertThrows<BusinessException> {
            useCase.execute(studentUserId, 1L, 10L, SelectTopicRequest(isSelected = false))
        }
    }

    @Test
    fun `본인 의뢰가 아니면 예외가 발생한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute("other-user-id-00000000001", 1L, 10L, SelectTopicRequest(isSelected = true))
        }
    }

    @Test
    fun `주제가 해당 의뢰에 속하지 않으면 예외가 발생한다`() {
        val commission = createCommission()
        val otherCommission = createCommission(id = 2L)
        val topic = createTopic(commission = otherCommission)

        every { commissionAdaptor.findById(1L) } returns commission
        every { commissionTopicAdaptor.findById(10L) } returns topic

        assertThrows<BusinessException> {
            useCase.execute(studentUserId, 1L, 10L, SelectTopicRequest(isSelected = true))
        }
    }

    @Test
    fun `TOPIC_PROPOSED 상태가 아니면 상태 전이 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.REQUESTED)
        val topic = createTopic(commission = commission)

        every { commissionAdaptor.findById(1L) } returns commission
        every { commissionTopicAdaptor.findById(10L) } returns topic
        every { lessonAdaptor.save(any()) } returns savedLesson(1L)

        assertThrows<IllegalArgumentException> {
            useCase.execute(studentUserId, 1L, 10L, SelectTopicRequest(isSelected = true))
        }
    }
}
