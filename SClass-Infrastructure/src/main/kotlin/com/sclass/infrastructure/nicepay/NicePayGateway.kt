package com.sclass.infrastructure.nicepay

import com.sclass.infrastructure.nicepay.dto.NicePayApproveResponse
import com.sclass.infrastructure.nicepay.dto.NicePayCancelResponse
import com.sclass.infrastructure.nicepay.dto.NicePayTokenResponse
import com.sclass.infrastructure.nicepay.dto.PgApproveResult
import com.sclass.infrastructure.nicepay.dto.PgCancelResult
import com.sclass.infrastructure.nicepay.exception.NicePayException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.Base64

@Component
class NicePayGateway(
    @param:Qualifier("nicePayWebClient") private val webClient: WebClient,
    private val properties: NicePayProperties,
) : PgGateway {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun getAccessToken(): String {
        val credentials =
            Base64
                .getEncoder()
                .encodeToString("${properties.clientKey}:${properties.secretKey}".toByteArray())

        val response =
            try {
                webClient
                    .post()
                    .uri("/v1/oauth/token")
                    .headers { it["Authorization"] = "Basic $credentials" }
                    .bodyValue(mapOf("grantType" to "client_credentials"))
                    .retrieve()
                    .bodyToMono(NicePayTokenResponse::class.java)
                    .block() ?: throw NicePayException("NicePay 토큰 응답이 비어있습니다")
            } catch (e: WebClientResponseException) {
                log.error("NicePay 토큰 발급 실패: ${e.message}")
                throw NicePayException("NicePay 토큰 발급 실패")
            }

        if (response.resultCode != RESULT_CODE_SUCCESS) {
            throw NicePayException("NicePay 토큰 발급 실패: ${response.resultMsg}")
        }

        return response.accessToken ?: throw NicePayException("NicePay 액세스 토큰이 없습니다")
    }

    override fun approve(
        pgOrderId: String,
        tid: String,
        amount: Int,
    ): PgApproveResult {
        val accessToken = getAccessToken()

        val response =
            try {
                webClient
                    .post()
                    .uri("/v1/payments/$tid")
                    .headers { it.setBearerAuth(accessToken) }
                    .bodyValue(mapOf("amount" to amount, "orderId" to pgOrderId))
                    .retrieve()
                    .bodyToMono(NicePayApproveResponse::class.java)
                    .block() ?: throw NicePayException("NicePay 승인 응답이 비어있습니다")
            } catch (e: WebClientResponseException) {
                log.error("NicePay 결제 승인 실패: ${e.message}")
                throw NicePayException("NicePay 결제 승인 실패")
            }

        if (response.resultCode != RESULT_CODE_SUCCESS) {
            throw NicePayException("NicePay 결제 승인 실패: ${response.resultMsg}")
        }

        return PgApproveResult(
            tid = response.tid ?: tid,
            pgOrderId = response.orderId ?: pgOrderId,
            amount = response.amount ?: amount,
        )
    }

    override fun cancel(
        tid: String,
        amount: Int,
        reason: String,
    ): PgCancelResult {
        val accessToken = getAccessToken()

        val response =
            try {
                webClient
                    .post()
                    .uri("/v1/payments/$tid/cancel")
                    .headers { it.setBearerAuth(accessToken) }
                    .bodyValue(mapOf("amount" to amount, "reason" to reason))
                    .retrieve()
                    .bodyToMono(NicePayCancelResponse::class.java)
                    .block() ?: throw NicePayException("NicePay 취소 응답이 비어있습니다")
            } catch (e: WebClientResponseException) {
                log.error("NicePay 결제 취소 실패: ${e.message}")
                throw NicePayException("NicePay 결제 취소 실패")
            }

        if (response.resultCode != RESULT_CODE_SUCCESS) {
            throw NicePayException("NicePay 결제 취소 실패: ${response.resultMsg}")
        }

        return PgCancelResult(
            tid = response.tid ?: tid,
            cancelAmount = response.cancelAmt ?: amount,
        )
    }

    companion object {
        private const val RESULT_CODE_SUCCESS = "0000"
    }
}
