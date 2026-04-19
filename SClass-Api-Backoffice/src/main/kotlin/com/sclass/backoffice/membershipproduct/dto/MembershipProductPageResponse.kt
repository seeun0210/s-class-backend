package com.sclass.backoffice.membershipproduct.dto

data class MembershipProductPageResponse(
    val content: List<MembershipProductResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)
