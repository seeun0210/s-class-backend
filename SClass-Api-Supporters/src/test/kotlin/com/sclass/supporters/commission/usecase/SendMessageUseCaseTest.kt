package com.sclass.supporters.commission.usecase

import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.MessageAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.Message
import com.sclass.domain.domains.commission.domain.OutputFormat
import com.sclass.supporters.commission.dto.SendMessageRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SendMessageUseCaseTest {
    private lateinit var commissionAdaptor: CommissionAdaptor
    private lateinit var messageAdaptor: MessageAdaptor
    private lateinit var useCase: SendMessageUseCase

    private val studentUserId = "student-user-id-0000000001"
    private val teacherUserId = "teacher-user-id-00000000001"

    @BeforeEach
    fun setUp() {
        commissionAdaptor = mockk()
        messageAdaptor = mockk()
        useCase = SendMessageUseCase(commissionAdaptor, messageAdaptor)
    }

    private fun createCommission(
        id: Long = 1L,
        status: CommissionStatus = CommissionStatus.REQUESTED,
    ) = Commission(
        id = id,
        studentUserId = studentUserId,
        teacherUserId = teacherUserId,
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

    @Test
    fun `선생님이 메시지를 보내면 ADDITIONAL_INFO_REQUESTED로 상태가 변경된다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        val messageSlot = slot<Message>()
        every { messageAdaptor.save(capture(messageSlot)) } answers { messageSlot.captured }

        val result = useCase.execute(teacherUserId, 1L, SendMessageRequest(content = "추가 자료 부탁드립니다"))

        assertAll(
            { assertEquals(teacherUserId, result.senderId) },
            { assertEquals("추가 자료 부탁드립니다", result.content) },
            { assertEquals(CommissionStatus.ADDITIONAL_INFO_REQUESTED, commission.status) },
        )
    }

    @Test
    fun `학생이 메시지를 보내면 REQUESTED로 상태가 변경된다`() {
        val commission = createCommission(status = CommissionStatus.ADDITIONAL_INFO_REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        val messageSlot = slot<Message>()
        every { messageAdaptor.save(capture(messageSlot)) } answers { messageSlot.captured }

        val result = useCase.execute(studentUserId, 1L, SendMessageRequest(content = "추가 자료 첨부합니다"))

        assertAll(
            { assertEquals(studentUserId, result.senderId) },
            { assertEquals("추가 자료 첨부합니다", result.content) },
            { assertEquals(CommissionStatus.REQUESTED, commission.status) },
        )
    }

    @Test
    fun `관련 없는 유저가 메시지를 보내면 예외가 발생한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute("other-user-id-00000000001", 1L, SendMessageRequest(content = "test"))
        }
    }

    @Test
    fun `상태 전이가 불가능하면 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.COMPLETED)
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<IllegalArgumentException> {
            useCase.execute(teacherUserId, 1L, SendMessageRequest(content = "test"))
        }
    }
}
