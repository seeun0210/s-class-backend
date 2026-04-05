package com.sclass.infrastructure.nicepay

import com.sclass.infrastructure.nicepay.dto.NicePayApproveResponse
import com.sclass.infrastructure.nicepay.dto.NicePayCancelResponse
import com.sclass.infrastructure.nicepay.dto.NicePayTokenResponse
import com.sclass.infrastructure.nicepay.exception.NicePayException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

class NicePayGatewayTest {
    private lateinit var webClient: WebClient
    private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec
    private lateinit var requestBodySpec: WebClient.RequestBodySpec
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var responseSpec: WebClient.ResponseSpec
    private lateinit var gateway: NicePayGateway

    private val properties =
        NicePayProperties(
            clientKey = "test-client-key",
            secretKey = "test-secret-key",
            baseUrl = "https://sandbox-api.nicepay.co.kr",
        )

    @BeforeEach
    fun setUp() {
        webClient = mockk()
        requestBodyUriSpec = mockk()
        requestBodySpec = mockk()
        requestHeadersSpec = mockk()
        responseSpec = mockk()
        gateway = NicePayGateway(webClient, properties)

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.headers(any()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
    }

    private fun mockTokenSuccess() {
        every {
            responseSpec.bodyToMono(NicePayTokenResponse::class.java)
        } returns Mono.just(NicePayTokenResponse(resultCode = "0000", resultMsg = "success", accessToken = "test-token"))
    }

    @Nested
    inner class Approve {
        @Test
        fun `결제 승인 성공 시 PgApproveResult를 반환한다`() {
            mockTokenSuccess()
            every {
                responseSpec.bodyToMono(NicePayApproveResponse::class.java)
            } returns
                Mono.just(
                    NicePayApproveResponse(
                        resultCode = "0000",
                        resultMsg = "success",
                        tid = "tid-001",
                        orderId = "order-001",
                        amount = 10000,
                    ),
                )

            val result = gateway.approve("order-001", "tid-001", 10000)

            assertAll(
                { assertEquals("tid-001", result.tid) },
                { assertEquals("order-001", result.pgOrderId) },
                { assertEquals(10000, result.amount) },
            )
        }

        @Test
        fun `토큰 발급 실패 시 NicePayException이 발생한다`() {
            every {
                responseSpec.bodyToMono(NicePayTokenResponse::class.java)
            } returns Mono.just(NicePayTokenResponse(resultCode = "9999", resultMsg = "인증 실패"))

            assertThrows<NicePayException> {
                gateway.approve("order-001", "tid-001", 10000)
            }
        }

        @Test
        fun `PG 승인 결과코드가 실패이면 NicePayException이 발생한다`() {
            mockTokenSuccess()
            every {
                responseSpec.bodyToMono(NicePayApproveResponse::class.java)
            } returns Mono.just(NicePayApproveResponse(resultCode = "2001", resultMsg = "카드 한도 초과"))

            assertThrows<NicePayException> {
                gateway.approve("order-001", "tid-001", 10000)
            }
        }

        @Test
        fun `PG API 호출 중 네트워크 오류 시 NicePayException이 발생한다`() {
            mockTokenSuccess()
            every {
                responseSpec.bodyToMono(NicePayApproveResponse::class.java)
            } returns
                Mono.error(
                    WebClientResponseException.create(500, "Internal Server Error", HttpHeaders.EMPTY, ByteArray(0), null),
                )

            assertThrows<NicePayException> {
                gateway.approve("order-001", "tid-001", 10000)
            }
        }
    }

    @Nested
    inner class Cancel {
        @Test
        fun `결제 취소 성공 시 PgCancelResult를 반환한다`() {
            mockTokenSuccess()
            every {
                responseSpec.bodyToMono(NicePayCancelResponse::class.java)
            } returns
                Mono.just(
                    NicePayCancelResponse(
                        resultCode = "0000",
                        resultMsg = "success",
                        tid = "tid-001",
                        cancelAmt = 10000,
                    ),
                )

            val result = gateway.cancel("tid-001", 10000, "고객 요청 취소")

            assertAll(
                { assertEquals("tid-001", result.tid) },
                { assertEquals(10000, result.cancelAmount) },
            )
        }

        @Test
        fun `PG 취소 결과코드가 실패이면 NicePayException이 발생한다`() {
            mockTokenSuccess()
            every {
                responseSpec.bodyToMono(NicePayCancelResponse::class.java)
            } returns Mono.just(NicePayCancelResponse(resultCode = "2001", resultMsg = "취소 불가"))

            assertThrows<NicePayException> {
                gateway.cancel("tid-001", 10000, "고객 요청 취소")
            }
        }

        @Test
        fun `PG API 호출 중 네트워크 오류 시 NicePayException이 발생한다`() {
            mockTokenSuccess()
            every {
                responseSpec.bodyToMono(NicePayCancelResponse::class.java)
            } returns
                Mono.error(
                    WebClientResponseException.create(500, "Internal Server Error", HttpHeaders.EMPTY, ByteArray(0), null),
                )

            assertThrows<NicePayException> {
                gateway.cancel("tid-001", 10000, "고객 요청 취소")
            }
        }
    }
}
