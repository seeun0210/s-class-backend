package com.sclass.backoffice.teacher.dto

data class TeacherPageResponse(
    val content: List<TeacherListResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)
