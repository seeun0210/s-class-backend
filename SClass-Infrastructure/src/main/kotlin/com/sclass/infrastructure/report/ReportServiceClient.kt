package com.sclass.infrastructure.report

import com.sclass.infrastructure.report.dto.CallbackConfig
import com.sclass.infrastructure.report.dto.CreateSurveyReportRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

@Component
@ConditionalOnProperty(prefix = "report-service", name = ["enabled"], havingValue = "true")
class ReportServiceClient(
    @Qualifier("reportServiceWebClient") private val webClient: WebClient,
) {
    fun createSurveyReport(
        requestId: String,
        studentName: String,
        answers: Map<String, Any>,
        callbackUrl: String,
        callbackSecret: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
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
            .toBodilessEntity()
            .timeout(Duration.ofSeconds(10))
            .subscribe(
                {
                    logger.info("[report-service] survey-report accepted requestId=$requestId status=${it.statusCode}")
                    onSuccess()
                },
                { e ->
                    val body = (e as? WebClientResponseException)?.responseBodyAsString ?: e.message
                    logger.error("[report-service] survey-report 호출 실패 requestId=$requestId body=$body")
                    onError(e)
                },
            )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReportServiceClient::class.java)
    }
}
