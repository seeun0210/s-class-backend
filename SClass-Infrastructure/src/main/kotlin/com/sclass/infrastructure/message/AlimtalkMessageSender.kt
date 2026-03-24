package com.sclass.infrastructure.message

import com.sclass.infrastructure.message.dto.AlimtalkRequest
import com.sclass.infrastructure.message.dto.AlimtalkResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
@ConditionalOnProperty(name = ["alimtalk.enabled"], havingValue = "true")
class AlimtalkMessageSender(
    @param:Qualifier("alimtalkWebClient") private val alimtalkWebClient: WebClient,
    private val alimtalkProperties: AlimtalkProperties,
) : MessageSender {
    companion object {
        private const val PHONE_VERIFY_TEMPLATE_CODE = "sclassPhoneVerify"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendVerificationCode(
        phoneNumber: String,
        code: String,
    ) {
        val timestamp = System.currentTimeMillis().toString()
        val url = "/alimtalk/v2/services/${alimtalkProperties.serviceId}/messages"
        val signature = makeSignature(url, timestamp)

        val requestBody =
            AlimtalkRequest(
                plusFriendId = alimtalkProperties.plusFriendId,
                templateCode = PHONE_VERIFY_TEMPLATE_CODE,
                messages =
                    listOf(
                        AlimtalkRequest.Message(
                            to = phoneNumber.replace("-", ""),
                            content = "[S-Class] 휴대전화 인증번호 안내\n\n인증번호는 $code 입니다. 5분 이내에 입력해주세요.\n\n본인이 요청하지 않았다면 이 메시지를 무시해주세요.",
                        ),
                    ),
            )
        val response =
            alimtalkWebClient
                .post()
                .uri(url)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("x-ncp-apigw-timestamp", timestamp)
                .header("x-ncp-iam-access-key", alimtalkProperties.accessKey)
                .header("x-ncp-apigw-signature-v2", signature)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono<AlimtalkResponse>()
                .block()

        if (response?.statusCode != "202") {
            log.error("[알림톡] 발송 실패: {}", response)
            throw RuntimeException("알림톡 발송에 실패했습니다")
        }
    }

    private fun makeSignature(
        url: String,
        timestamp: String,
    ): String {
        val message = "${HttpMethod.POST.name()} $url\n$timestamp\n${alimtalkProperties.accessKey}"

        val signingKey =
            SecretKeySpec(
                alimtalkProperties.secretKey.toByteArray(Charsets.UTF_8),
                "HmacSHA256",
            )
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)

        val rawHmac = mac.doFinal(message.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(rawHmac)
    }
}
