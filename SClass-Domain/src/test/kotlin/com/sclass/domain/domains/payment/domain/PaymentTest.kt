package com.sclass.domain.domains.payment.domain

import com.sclass.domain.domains.payment.exception.InvalidPaymentStatusException
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentTest {
    private fun createPayment(status: PaymentStatus = PaymentStatus.PENDING) =
        Payment(
            userId = "user-id-00000000000",
            targetType = PaymentTargetType.COIN_PACKAGE,
            targetId = "coin-pkg-id-0001",
            amount = 10000,
            pgType = PgType.NICEPAY,
            pgOrderId = "order-001",
        ).also {
            when (status) {
                PaymentStatus.PG_APPROVED -> it.markPgApproved("tid-001")
                PaymentStatus.COMPLETED -> {
                    it.markPgApproved("tid-001")
                    it.markCompleted()
                }
                PaymentStatus.PG_APPROVE_FAILED -> it.markPgApproveFailed()
                else -> {}
            }
        }

    @Nested
    inner class MarkPgApproved {
        @Test
        fun `PENDING 상태에서 PG 승인 시 PG_APPROVED로 변경되고 pgTid가 저장된다`() {
            val payment = createPayment()

            payment.markPgApproved("tid-001")

            assertAll(
                { assertEquals(PaymentStatus.PG_APPROVED, payment.status) },
                { assertEquals("tid-001", payment.pgTid) },
            )
        }

        @Test
        fun `PENDING이 아닌 상태에서 PG 승인 시 예외가 발생한다`() {
            val payment = createPayment(PaymentStatus.PG_APPROVED)

            assertThrows<InvalidPaymentStatusException> {
                payment.markPgApproved("tid-002")
            }
        }
    }

    @Nested
    inner class MarkCompleted {
        @Test
        fun `PG_APPROVED 상태에서 완료 처리 시 COMPLETED로 변경된다`() {
            val payment = createPayment(PaymentStatus.PG_APPROVED)

            payment.markCompleted()

            assertEquals(PaymentStatus.COMPLETED, payment.status)
        }

        @Test
        fun `PG_APPROVED가 아닌 상태에서 완료 처리 시 예외가 발생한다`() {
            val payment = createPayment()

            assertThrows<InvalidPaymentStatusException> {
                payment.markCompleted()
            }
        }
    }

    @Nested
    inner class MarkCancelled {
        @Test
        fun `COMPLETED 상태에서 취소 시 CANCELLED로 변경된다`() {
            val payment = createPayment(PaymentStatus.COMPLETED)

            payment.markCancelled()

            assertEquals(PaymentStatus.CANCELLED, payment.status)
        }

        @Test
        fun `PENDING 상태에서 취소 시 CANCELLED로 변경된다`() {
            val payment = createPayment()

            payment.markCancelled()

            assertEquals(PaymentStatus.CANCELLED, payment.status)
        }

        @Test
        fun `PG_APPROVED 상태에서 취소 시 CANCELLED로 변경된다`() {
            val payment = createPayment(PaymentStatus.PG_APPROVED)

            payment.markCancelled()

            assertEquals(PaymentStatus.CANCELLED, payment.status)
        }

        @Test
        fun `PG_APPROVE_FAILED 상태에서 취소 시 예외가 발생한다`() {
            val payment = createPayment(PaymentStatus.PG_APPROVE_FAILED)

            assertThrows<InvalidPaymentStatusException> {
                payment.markCancelled()
            }
        }
    }

    @Nested
    inner class MarkPgApproveFailed {
        @Test
        fun `PENDING 상태에서 PG 승인 실패 시 PG_APPROVE_FAILED로 변경된다`() {
            val payment = createPayment()

            payment.markPgApproveFailed()

            assertEquals(PaymentStatus.PG_APPROVE_FAILED, payment.status)
        }

        @Test
        fun `PENDING이 아닌 상태에서 PG 승인 실패 처리 시 예외가 발생한다`() {
            val payment = createPayment(PaymentStatus.PG_APPROVED)

            assertThrows<InvalidPaymentStatusException> {
                payment.markPgApproveFailed()
            }
        }
    }

    @Nested
    inner class MarkIssueCoinFailed {
        @Test
        fun `PG_APPROVED 상태에서 코인 발급 실패 시 ISSUE_COIN_FAILED로 변경된다`() {
            val payment = createPayment(PaymentStatus.PG_APPROVED)

            payment.markIssueCoinFailed()

            assertEquals(PaymentStatus.ISSUE_COIN_FAILED, payment.status)
        }

        @Test
        fun `PG_APPROVED가 아닌 상태에서 코인 발급 실패 처리 시 예외가 발생한다`() {
            val payment = createPayment()

            assertThrows<InvalidPaymentStatusException> {
                payment.markIssueCoinFailed()
            }
        }
    }
}
