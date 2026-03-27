package com.sclass.backoffice.student.dto

data class StudentPageResponse(
    val content: List<StudentListResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)
