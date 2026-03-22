package com.sclass.backoffice.organization.dto

data class OrganizationUserStatsResponse(
    val totalCount: Long,
    val adminCount: Long,
    val teacherCount: Long,
    val studentCount: Long,
)
