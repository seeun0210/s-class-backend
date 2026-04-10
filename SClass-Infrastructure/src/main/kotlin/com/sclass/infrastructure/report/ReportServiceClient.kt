package com.sclass.infrastructure.report

import com.sclass.infrastructure.report.dto.CallbackConfig
import com.sclass.infrastructure.report.dto.CreateReportRequest
import com.sclass.infrastructure.report.dto.CreateReportResponse
import com.sclass.infrastructure.report.dto.CreateSurveyReportRequest
import com.sclass.infrastructure.report.dto.ReportListResponse
import com.sclass.infrastructure.report.dto.ReportStateDto
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
    private val properties: ReportServiceProperties,
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
            .timeout(Duration.ofSeconds(properties.timeoutSeconds))
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

    fun createReport(
        requestId: String,
        paragraph: String,
    ): String {
        val request =
            CreateReportRequest(
                requestId = requestId,
                paragraph = paragraph,
                callback =
                    CallbackConfig(
                        url = "${properties.callbackBaseUrl}/internal/webhooks/report",
                        secret = properties.callbackSecret,
                    ),
            )
        val response =
            webClient
                .post()
                .uri("/api/v2/reports")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CreateReportResponse::class.java)
                .timeout(Duration.ofSeconds(properties.timeoutSeconds))
                .block() ?: throw IllegalStateException("ReportService 응답 없음 requestId=$requestId")
        logger.info("[report-service] report created requestId=$requestId jobId=${response.jobId}")
        return response.jobId
    }

    fun getReports(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
    ): ReportListResponse? =
        webClient
            .get()
            .uri {
                it
                    .path("/api/v2/reports")
                    .queryParam("page", page)
                    .queryParam("limit", limit)
                    .apply { if (search != null) queryParam("search", search) }
                    .build()
            }.retrieve()
            .bodyToMono(ReportListResponse::class.java)
            .timeout(Duration.ofSeconds(properties.timeoutSeconds))
            .block()

    fun getReportByJobId(jobId: String): ReportStateDto? =
        webClient
            .get()
            .uri("/api/v2/reports/jobs/$jobId/result")
            .retrieve()
            .bodyToMono(ReportStateDto::class.java)
            .timeout(Duration.ofSeconds(properties.timeoutSeconds))
            .block()

    companion object {
        private val logger = LoggerFactory.getLogger(ReportServiceClient::class.java)
    }
}
