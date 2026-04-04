package com.sclass.domain.domains.webhook.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class WebhookFieldMapping(
    @Column(name = "student_name_question", nullable = false)
    val studentNameQuestion: String,

    @Column(name = "student_phone_question", nullable = false)
    val studentPhoneQuestion: String,

    @Column(name = "parent_phone_question")
    val parentPhoneQuestion: String?,
) {
    fun extractFrom(answers: Map<String, Any>): ExtractedFields {
        val studentName =
            answers[studentNameQuestion]?.toString()
                ?: throw IllegalArgumentException("학생 이름 필드를 찾을 수 없습니다: $studentNameQuestion")

        return ExtractedFields(
            studentName = studentName,
            studentPhone = answers[studentPhoneQuestion]?.toString()?.replace("-", ""),
            parentPhone = parentPhoneQuestion?.let { answers[it] }?.toString()?.replace("-", ""),
        )
    }
}

data class ExtractedFields(
    val studentName: String,
    val studentPhone: String?,
    val parentPhone: String?,
)
