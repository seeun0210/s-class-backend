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
import com.sclass.supporters.commission.dto.ProposeTopicsRequest
import com.sclass.supporters.commission.dto.TopicRequest
import com.sclass.supporters.commission.scheduler.CommissionReminderScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher

class ProposeTopicsUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var commissionTopicAdaptor: CommissionTopicAdaptor
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var commissionReminderScheduler: CommissionReminderScheduler
    private lateinit var useCase: ProposeTopicsUseCase

    private val teacherUserId = "teacher-user-id-00000000001"
    private val studentUserId = "student-user-id-0000000001"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        commissionTopicAdaptor = mockk()
        eventPublisher = mockk(relaxed = true)
        commissionReminderScheduler = mockk(relaxed = true)
        useCase = ProposeTopicsUseCase(commissionAdaptor, commissionTopicAdaptor, eventPublisher, commissionReminderScheduler)
    }

    private fun createCommission(
        id: Long = 1L,
        status: CommissionStatus = CommissionStatus.REQUESTED,
    ) = Commission(
        id = id,
        studentUserId = studentUserId,
        teacherUserId = teacherUserId,
        productId = "product-id-0000000000000001",
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

    private fun createRequest() =
        ProposeTopicsRequest(
            topics =
                listOf(
                    TopicRequest(topicId = "mongo-id-001", title = "주제 1", description = "설명 1"),
                    TopicRequest(topicId = "mongo-id-002", title = "주제 2", description = null),
                ),
        )

    @Test
    fun `선생님이 주제를 제안하면 CommissionTopicListResponse를 반환한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        val topicsSlot = slot<List<CommissionTopic>>()
        every { commissionTopicAdaptor.saveAll(capture(topicsSlot)) } answers { topicsSlot.captured }

        val result = useCase.execute(teacherUserId, 1L, createRequest())

        assertAll(
            { assertEquals(2, result.topics.size) },
            { assertEquals("주제 1", result.topics[0].title) },
            { assertEquals("mongo-id-001", result.topics[0].topicId) },
            { assertEquals("설명 1", result.topics[0].description) },
            { assertEquals("주제 2", result.topics[1].title) },
            { assertEquals(null, result.topics[1].description) },
            { assertEquals(CommissionStatus.TOPIC_PROPOSED, commission.status) },
        )
        verify { commissionTopicAdaptor.saveAll(match { it.size == 2 }) }
    }

    @Test
    fun `담당 선생님이 아니면 예외가 발생한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute("other-user-id-00000000001", 1L, createRequest())
        }
    }

    @Test
    fun `REQUESTED 상태가 아니면 상태 전이 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.TOPIC_PROPOSED)
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<IllegalArgumentException> {
            useCase.execute(teacherUserId, 1L, createRequest())
        }
    }
}
