package com.sclass.domain.domains.commission.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class GuideInfo(
    @Column(name = "guide_subject", nullable = false)
    val subject: String,

    @Column(name = "guide_volume", nullable = false)
    val volume: String,

    @Column(name = "guide_required_elements")
    val requiredElements: String? = null,

    @Column(name = "guide_grading_criteria", nullable = false, columnDefinition = "TEXT")
    val gradingCriteria: String,

    @Column(name = "guide_teacher_emphasis", nullable = false, columnDefinition = "TEXT")
    val teacherEmphasis: String,
)
