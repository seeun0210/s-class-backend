package com.sclass.backoffice.teacherassignment.dto

data class TeacherAssignmentPageResponse(
    val content: List<TeacherAssignmentListResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)
