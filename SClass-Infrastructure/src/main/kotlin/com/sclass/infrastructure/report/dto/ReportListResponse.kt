package com.sclass.infrastructure.report.dto

data class ReportListResponse(
    val items: List<ReportStateDto>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
)
