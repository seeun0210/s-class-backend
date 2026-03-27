package com.sclass.backoffice.student.dto

data class BulkCreateStudentsResponse(
    val totalCount: Int,
    val successCount: Int,
    val failureCount: Int,
    val results: List<BulkCreateStudentResult>,
)

data class BulkCreateStudentResult(
    val row: Int,
    val email: String,
    val success: Boolean,
    val data: CreateStudentResponse? = null,
    val error: String? = null,
)
