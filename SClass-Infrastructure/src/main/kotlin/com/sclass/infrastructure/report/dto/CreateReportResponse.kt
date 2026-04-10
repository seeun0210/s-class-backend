package com.sclass.infrastructure.report.dto

data class CreateReportResponse(
    val jobId: String,
    val topic: String? = null,
)
