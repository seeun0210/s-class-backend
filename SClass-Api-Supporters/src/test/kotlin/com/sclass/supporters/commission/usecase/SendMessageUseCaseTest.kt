package com.sclass.supporters.commission.usecase

import com.sclass.common.exception.BusinessException
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.MessageAdaptor
import com.sclass.domain.domains.commission.domain.ActivityType
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionStatus
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.commission.domain.Message
import com.sclass.domain.domains.commission.domain.MessageType
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

    // --- 선생님 추가 정보 요청 (REQUESTED → ADDITIONAL_INFO_REQUESTED) ---

    @Test
    fun `선생님이 REQUESTED에서 추가 정보 요청하면 ADDITIONAL_INFO_REQUESTED로 변경된다`() {
        val commission = createCommission(status = CommissionStatus.REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        val messageSlot = slot<Message>()
        every { messageAdaptor.save(capture(messageSlot)) } answers { messageSlot.captured }

        val result =
            useCase.execute(
                teacherUserId,
                1L,
                SendMessageRequest(type = MessageType.ADDITIONAL_INFO_REQUEST, content = "추가 자료 부탁드립니다"),
            )

        assertAll(
            { assertEquals(teacherUserId, result.senderId) },
            { assertEquals(MessageType.ADDITIONAL_INFO_REQUEST, result.type) },
            { assertEquals("추가 자료 부탁드립니다", result.content) },
            { assertEquals(CommissionStatus.ADDITIONAL_INFO_REQUESTED, commission.status) },
        )
    }

    // --- 학생 추가 정보 응답 (ADDITIONAL_INFO_REQUESTED → REQUESTED) ---

    @Test
    fun `학생이 ADDITIONAL_INFO_REQUESTED에서 응답하면 REQUESTED로 변경된다`() {
        val commission = createCommission(status = CommissionStatus.ADDITIONAL_INFO_REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        val messageSlot = slot<Message>()
        every { messageAdaptor.save(capture(messageSlot)) } answers { messageSlot.captured }

        val result =
            useCase.execute(
                studentUserId,
                1L,
                SendMessageRequest(type = MessageType.ADDITIONAL_INFO_RESPONSE, content = "추가 자료 첨부합니다"),
            )

        assertAll(
            { assertEquals(studentUserId, result.senderId) },
            { assertEquals(MessageType.ADDITIONAL_INFO_RESPONSE, result.type) },
            { assertEquals(CommissionStatus.REQUESTED, commission.status) },
        )
    }

    // --- 학생 추가 자료 요청 (TOPIC_SELECTED, IN_PROGRESS → 상태 변경 없음) ---

    @Test
    fun `학생이 TOPIC_SELECTED에서 추가 자료 요청하면 상태 변경 없이 메시지가 저장된다`() {
        val commission = createCommission(status = CommissionStatus.TOPIC_SELECTED)
        every { commissionAdaptor.findById(1L) } returns commission

        val messageSlot = slot<Message>()
        every { messageAdaptor.save(capture(messageSlot)) } answers { messageSlot.captured }

        val result =
            useCase.execute(
                studentUserId,
                1L,
                SendMessageRequest(type = MessageType.ADDITIONAL_INFO_REQUEST, content = "이 부분 설명해주세요"),
            )

        assertAll(
            { assertEquals(MessageType.ADDITIONAL_INFO_REQUEST, result.type) },
            { assertEquals(CommissionStatus.TOPIC_SELECTED, commission.status) },
        )
    }

    @Test
    fun `학생이 IN_PROGRESS에서 추가 자료 요청하면 상태 변경 없이 메시지가 저장된다`() {
        val commission = createCommission(status = CommissionStatus.IN_PROGRESS)
        every { commissionAdaptor.findById(1L) } returns commission

        val messageSlot = slot<Message>()
        every { messageAdaptor.save(capture(messageSlot)) } answers { messageSlot.captured }

        val result =
            useCase.execute(
                studentUserId,
                1L,
                SendMessageRequest(type = MessageType.ADDITIONAL_INFO_REQUEST, content = "추가 자료 부탁드려요"),
            )

        assertAll(
            { assertEquals(MessageType.ADDITIONAL_INFO_REQUEST, result.type) },
            { assertEquals(CommissionStatus.IN_PROGRESS, commission.status) },
        )
    }

    // --- 선생님 응답 (TOPIC_SELECTED, IN_PROGRESS → 상태 변경 없음) ---

    @Test
    fun `선생님이 TOPIC_SELECTED에서 응답하면 상태 변경 없이 메시지가 저장된다`() {
        val commission = createCommission(status = CommissionStatus.TOPIC_SELECTED)
        every { commissionAdaptor.findById(1L) } returns commission

        val messageSlot = slot<Message>()
        every { messageAdaptor.save(capture(messageSlot)) } answers { messageSlot.captured }

        val result =
            useCase.execute(
                teacherUserId,
                1L,
                SendMessageRequest(type = MessageType.ADDITIONAL_INFO_RESPONSE, content = "확인했습니다"),
            )

        assertAll(
            { assertEquals(MessageType.ADDITIONAL_INFO_RESPONSE, result.type) },
            { assertEquals(CommissionStatus.TOPIC_SELECTED, commission.status) },
        )
    }

    // --- 허용되지 않은 상태에서 메시지 전송 시 예외 ---

    @Test
    fun `선생님이 TOPIC_SELECTED에서 추가 정보 요청하면 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.TOPIC_SELECTED)
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(
                teacherUserId,
                1L,
                SendMessageRequest(type = MessageType.ADDITIONAL_INFO_REQUEST, content = "test"),
            )
        }
    }

    @Test
    fun `학생이 REQUESTED에서 응답하면 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(
                studentUserId,
                1L,
                SendMessageRequest(type = MessageType.ADDITIONAL_INFO_RESPONSE, content = "test"),
            )
        }
    }

    @Test
    fun `COMPLETED 상태에서 메시지를 보내면 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.COMPLETED)
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(
                teacherUserId,
                1L,
                SendMessageRequest(type = MessageType.ADDITIONAL_INFO_REQUEST, content = "test"),
            )
        }
    }

    @Test
    fun `REJECTION_REASON 타입으로 메시지를 보내면 예외가 발생한다`() {
        val commission = createCommission(status = CommissionStatus.REQUESTED)
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(
                teacherUserId,
                1L,
                SendMessageRequest(type = MessageType.REJECTION_REASON, content = "test"),
            )
        }
    }

    // --- 권한 ---

    @Test
    fun `관련 없는 유저가 메시지를 보내면 예외가 발생한다`() {
        val commission = createCommission()
        every { commissionAdaptor.findById(1L) } returns commission

        assertThrows<BusinessException> {
            useCase.execute(
                "other-user-id-00000000001",
                1L,
                SendMessageRequest(type = MessageType.ADDITIONAL_INFO_REQUEST, content = "test"),
            )
        }
    }
}
