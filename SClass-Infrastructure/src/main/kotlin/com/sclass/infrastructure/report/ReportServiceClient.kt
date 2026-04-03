package com.sclass.infrastructure.report

import com.sclass.infrastructure.report.dto.CallbackConfig
import com.sclass.infrastructure.report.dto.CreateSurveyReportRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
@ConditionalOnProperty(prefix = "report-service", name = ["enabled"], havingValue = "true")
class ReportServiceClient(
    @Qualifier("reportServiceWebClient") private val webClient: WebClient,
) {
    fun createSurveyReport(
        requestId: String,
        studentName: String,
        answers: Map<String, String>,
        callbackUrl: String,
        callbackSecret: String,
    ) {
        val request =
            CreateSurveyReportRequest(
                requestId = requestId,
                studentName = studentName,
                answers = answers,
                callback =
                    CallbackConfig(
                        url = callbackUrl,
                        secret = callbackSecret,
                    ),
            )
        webClient
            .post()
            .uri("/survey-report")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String::class.java)
            .timeout(Duration.ofSeconds(30))
            .subscribe(
                { /* 동기 응답 무시, 결과는 콜백으로 */ },
                { e -> logger.error("[report-service] 호출 실패: ${e.message}") },
            )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReportServiceClient::class.java)
    }
}
