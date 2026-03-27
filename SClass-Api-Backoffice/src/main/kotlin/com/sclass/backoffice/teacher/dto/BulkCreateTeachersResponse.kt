package com.sclass.backoffice.teacher.dto

data class BulkCreateTeachersResponse(
    val totalCount: Int,
    val successCount: Int,
    val failureCount: Int,
    val results: List<BulkCreateTeacherResult>,
)

data class BulkCreateTeacherResult(
    val row: Int,
    val email: String,
    val success: Boolean,
    val data: CreateTeacherResponse? = null,
    val error: String? = null,
)
