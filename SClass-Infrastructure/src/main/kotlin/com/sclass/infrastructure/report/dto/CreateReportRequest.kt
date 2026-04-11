package com.sclass.infrastructure.report.dto

data class CreateReportRequest(
    val requestId: String,
    val paragraph: String,
    val callback: CallbackConfig,
)
