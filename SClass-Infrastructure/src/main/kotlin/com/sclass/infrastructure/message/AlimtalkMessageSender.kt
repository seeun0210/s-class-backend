package com.sclass.infrastructure.message

import com.sclass.infrastructure.message.dto.AlimtalkRequest
import com.sclass.infrastructure.message.dto.AlimtalkResponse
import com.sclass.infrastructure.message.dto.AlimtalkTemplate
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
) : VerificationCodeSender,
    CommissionNotificationSender {
    private val commissionTemplates = CommissionAlimtalkTemplates(alimtalkProperties.appBaseUrl)

    override fun sendVerificationCode(
        phoneNumber: String,
        code: String,
    ) = sendTemplate(phoneNumber, VerificationTemplate.verificationCode(code))

    override fun sendCommissionAssigned(
        phoneNumber: String,
        teacherName: String,
        studentName: String,
        subject: String,
        createdAt: String,
        commissionId: String,
    ) = sendTemplate(
        phoneNumber,
        commissionTemplates.commissionAssigned(
            teacherName,
            studentName,
            subject,
            createdAt,
            commissionId,
        ),
    )

    override fun sendTopicSuggested(
        phoneNumber: String,
        studentName: String,
        commissionId: String,
    ) = sendTemplate(phoneNumber, commissionTemplates.topicSuggested(studentName, commissionId))

    override fun sendAdditionalInfoRequested(
        phoneNumber: String,
        studentName: String,
        requestContent: String,
        commissionId: String,
    ) = sendTemplate(phoneNumber, commissionTemplates.additionalInfoRequested(studentName, requestContent, commissionId))

    override fun sendTicketResolved(
        phoneNumber: String,
        teacherName: String,
        ticketType: String,
        commissionId: String,
    ) = sendTemplate(phoneNumber, commissionTemplates.ticketResolved(teacherName, ticketType, commissionId))

    override fun sendNoResponseReminder(
        phoneNumber: String,
        teacherName: String,
        studentName: String,
        elapsedTime: String,
        commissionId: String,
    ) = sendTemplate(
        phoneNumber,
        commissionTemplates.noResponseReminder(
            teacherName,
            studentName,
            elapsedTime,
            commissionId,
        ),
    )

    override fun sendInactivityReminder(
        phoneNumber: String,
        teacherName: String,
        studentName: String,
        inactiveDays: Int,
        lastActivityAt: String,
        commissionId: String,
    ) = sendTemplate(
        phoneNumber,
        commissionTemplates.inactivityReminder(
            teacherName,
            studentName,
            inactiveDays,
            lastActivityAt,
            commissionId,
        ),
    )

    private fun sendTemplate(
        to: String,
        template: AlimtalkTemplate,
    ) = send(to = to, templateCode = template.templateCode, content = template.content, buttons = template.buttons)

    private fun send(
        to: String,
        templateCode: String,
        content: String,
        buttons: List<AlimtalkRequest.Button>? = null,
    ) {
        val timestamp = System.currentTimeMillis().toString()
        val url = "/alimtalk/v2/services/${alimtalkProperties.serviceId}/messages"
        val signature = makeSignature(url, timestamp)

        val requestBody =
            AlimtalkRequest(
                plusFriendId = alimtalkProperties.plusFriendId,
                templateCode = templateCode,
                messages =
                    listOf(
                        AlimtalkRequest.Message(
                            to = to.replace("-", ""),
                            content = content,
                            buttons = buttons,
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
            throw RuntimeException("알림톡 발송에 실패했습니다: $response")
        }
    }

    private fun makeSignature(
        url: String,
        timestamp: String,
    ): String {
        val message = "${HttpMethod.POST.name()} $url\n$timestamp\n${alimtalkProperties.accessKey}"
        val signingKey = SecretKeySpec(alimtalkProperties.secretKey.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        return Base64.getEncoder().encodeToString(mac.doFinal(message.toByteArray(Charsets.UTF_8)))
    }
}
