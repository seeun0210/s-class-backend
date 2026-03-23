package com.sclass.infrastructure.email

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["email.smtp.enabled"], havingValue = "true")
class SmtpEmailSender(
    private val mailSender: JavaMailSender,
) : EmailSender {
    companion object {
        private const val SUBJECT = "[S-Class] 이메일 인증번호"
    }

    override fun sendVerificationCode(
        email: String,
        code: String,
        serviceName: String,
    ) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, false, "UTF-8")
        helper.setTo(email)
        helper.setSubject(SUBJECT)
        helper.setText(buildHtml(code, serviceName), true)
        mailSender.send(message)
    }

    private fun buildHtml(
        code: String,
        serviceName: String,
    ): String =
        """
        |<!DOCTYPE html>
        |<html>
        |<head><meta charset="UTF-8"></head>
        |<body style="margin:0;padding:0;font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif;background-color:#f4f4f4;">
        |  <table width="100%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
        |    <tr><td align="center">
        |      <table width="480" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;padding:40px;">
        |        <tr><td style="font-size:20px;font-weight:bold;color:#333;padding-bottom:8px;">
        |          $serviceName 이메일 인증
        |        </td></tr>
        |        <tr><td style="font-size:14px;color:#666;padding-bottom:24px;">
        |          아래 인증번호를 입력해주세요.
        |        </td></tr>
        |        <tr><td align="center" style="padding:20px 0;">
        |          <span style="display:inline-block;font-size:32px;font-weight:bold;letter-spacing:8px;color:#333;
        |                       background:#f0f0f0;padding:16px 32px;border-radius:8px;">
        |            $code
        |          </span>
        |        </td></tr>
        |        <tr><td style="font-size:13px;color:#999;padding-top:24px;">
        |          이 인증번호는 5분간 유효합니다.<br>
        |          본인이 요청하지 않았다면 이 메일을 무시해주세요.
        |        </td></tr>
        |      </table>
        |    </td></tr>
        |  </table>
        |</body>
        |</html>
        """.trimMargin()
}
