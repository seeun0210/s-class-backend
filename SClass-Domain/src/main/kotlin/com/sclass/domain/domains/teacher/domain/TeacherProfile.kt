package com.sclass.domain.domains.teacher.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDate

@Embeddable
data class TeacherProfile(
    @Column
    val birthDate: LocalDate? = null,

    @Column(length = 1000)
    val selfIntroduction: String? = null,
)
