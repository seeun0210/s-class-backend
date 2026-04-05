package com.sclass.infrastructure.nicepay

import com.sclass.infrastructure.nicepay.dto.NicePayApproveResponse
import com.sclass.infrastructure.nicepay.dto.NicePayCancelResponse
import com.sclass.infrastructure.nicepay.dto.NicePayInquiryResponse
import com.sclass.infrastructure.nicepay.dto.NicePayTokenResponse
import com.sclass.infrastructure.nicepay.dto.PgApproveResult
import com.sclass.infrastructure.nicepay.dto.PgCancelResult
import com.sclass.infrastructure.nicepay.dto.PgInquiryResult
import com.sclass.infrastructure.nicepay.exception.NicePayException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.security.MessageDigest
import java.time.Instant

@Component
class NicePayGateway(
    @param:Qualifier("nicePayWebClient") private val webClient: WebClient,
    private val properties: NicePayProperties,
) : PgGateway {
    private val log = LoggerFactory.getLogger(javaClass)

    private var cachedToken: String? = null
    private var tokenExpiresAt: Instant = Instant.EPOCH

    @Synchronized
    private fun getAccessToken(): String {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken!!
        }

        val response =
            try {
                webClient
                    .post()
                    .uri("/v1/oauth/token")
                    .headers { it.setBasicAuth(properties.clientKey, properties.secretKey) }
                    .bodyValue(mapOf("grantType" to "client_credentials"))
                    .retrieve()
                    .bodyToMono(NicePayTokenResponse::class.java)
                    .block() ?: throw NicePayException("NicePay 토큰 응답이 비어있습니다")
            } catch (e: NicePayException) {
                throw e
            } catch (e: Exception) {
                log.error("NicePay 토큰 발급 실패: ${e.message}", e)
                throw NicePayException("NicePay 토큰 발급 실패", e)
            }

        if (response.resultCode != RESULT_CODE_SUCCESS) {
            throw NicePayException("NicePay 토큰 발급 실패: ${response.resultMsg}")
        }

        val token = response.accessToken ?: throw NicePayException("NicePay 액세스 토큰이 없습니다")
        val expiresIn = response.expiresIn ?: DEFAULT_TOKEN_EXPIRES_IN

        cachedToken = token
        tokenExpiresAt = Instant.now().plusSeconds(expiresIn - TOKEN_EXPIRY_BUFFER_SECONDS)

        return token
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
            } catch (e: NicePayException) {
                throw e
            } catch (e: Exception) {
                log.error("NicePay 결제 승인 실패: ${e.message}", e)
                throw NicePayException("NicePay 결제 승인 실패", e)
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
            } catch (e: NicePayException) {
                throw e
            } catch (e: Exception) {
                log.error("NicePay 결제 취소 실패: ${e.message}", e)
                throw NicePayException("NicePay 결제 취소 실패", e)
            }

        if (response.resultCode != RESULT_CODE_SUCCESS) {
            throw NicePayException("NicePay 결제 취소 실패: ${response.resultMsg}")
        }

        return PgCancelResult(
            tid = response.tid ?: tid,
            cancelAmount = response.cancelAmt ?: amount,
        )
    }

    override fun verifyWebhookSignature(
        tid: String,
        amount: Int,
        ediDate: String,
        signature: String,
    ): Boolean {
        val raw = "$tid$amount$ediDate${properties.secretKey}"
        val expected =
            MessageDigest
                .getInstance("SHA-256")
                .digest(raw.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        return expected == signature
    }

    override fun inquiry(pgOrderId: String): PgInquiryResult {
        val accessToken = getAccessToken()

        val response =
            try {
                webClient
                    .get()
                    .uri("/v1/payments?orderId=$pgOrderId")
                    .headers { it.setBearerAuth(accessToken) }
                    .retrieve()
                    .bodyToMono(NicePayInquiryResponse::class.java)
                    .block() ?: throw NicePayException("NicePay 조회 응답이 비어있습니다")
            } catch (e: NicePayException) {
                throw e
            } catch (e: Exception) {
                log.error("NicePay 거래 조회 실패: ${e.message}", e)
                throw NicePayException("NicePay 거래 조회 실패", e)
            }

        return PgInquiryResult(
            approved = response.resultCode == RESULT_CODE_SUCCESS,
            tid = response.tid,
            amount = response.amount ?: 0,
        )
    }

    override fun verifyReturnSignature(
        authToken: String,
        amount: Int,
        signature: String,
    ): Boolean {
        val raw = "$authToken${properties.clientKey}$amount${properties.secretKey}"
        val expected =
            MessageDigest
                .getInstance("SHA-256")
                .digest(raw.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        return MessageDigest.isEqual(expected.toByteArray(Charsets.UTF_8), signature.toByteArray(Charsets.UTF_8))
    }

    companion object {
        private const val RESULT_CODE_SUCCESS = "0000"
        private const val DEFAULT_TOKEN_EXPIRES_IN = 1800L
        private const val TOKEN_EXPIRY_BUFFER_SECONDS = 60L
    }
}
