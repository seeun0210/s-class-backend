package com.sclass.domain.domains.teacher.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class TeacherEducation(
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    val majorCategory: MajorCategory? = null,

    @Column(length = 100)
    val university: String? = null,

    @Column(length = 100)
    val major: String? = null,

    @Column(length = 100)
    val highSchool: String? = null,
)
