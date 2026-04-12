package com.sclass.infrastructure.report.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ReportStateDto(
    val id: String? = null,
    val jobId: String? = null,
    val paragraph: String? = null,
    val topic: String? = null,

    @param:JsonProperty("keywords_core") val keywordsCore: List<String>? = null,
    @param:JsonProperty("keywords_related") val keywordsRelated: List<String>? = null,
    val keywords: List<String>? = null,

    val subject: String? = null,
    val category: String? = null,
    val major: String? = null,
    val difficulty: String? = null,
    val reason: String? = null,

    @param:JsonProperty("related_subjects") val relatedSubjects: List<String>? = null,

    val index: ReportIndexDto? = null,
    val motivation: Map<String, Any?>? = null,
    val citations: List<Map<String, Any?>>? = null,
    val activities: Map<String, Any?>? = null,
    val summary: Map<String, Any?>? = null,
    val extraTools: Map<String, Any?>? = null,

    val createdAt: String? = null,
    val status: String? = null,
    val schemaVersion: Int? = null,
)

data class ReportIndexDto(
    val topic: String? = null,

    @param:JsonProperty("keywords_core") val keywordsCore: List<String>? = null,
    @param:JsonProperty("keywords_related") val keywordsRelated: List<String>? = null,
    val keywords: List<String>? = null,

    val subject: String? = null,
    val category: String? = null,
    val major: String? = null,
    val difficulty: String? = null,
    val reason: String? = null,

    @param:JsonProperty("related_subjects") val relatedSubjects: List<String>? = null,
)
