package com.sclass.infrastructure.report.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ReportStateDto(
    val id: String,
    val jobId: String,
    val paragraph: String,
    val topic: String? = null,

    @param:JsonProperty("keywords_core") val keywordsCore: List<String> = emptyList(),
    @param:JsonProperty("keywords_related") val keywordsRelated: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),

    val subject: String? = null,
    val category: String? = null,
    val major: String? = null,
    val difficulty: String? = null,
    val reason: String? = null,

    @param:JsonProperty("related_subjects") val relatedSubjects: List<String> = emptyList(),

    val createdAt: String? = null,
    val status: String? = null,
    val schemaVersion: Int = 2,
)
