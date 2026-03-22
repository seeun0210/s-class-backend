package com.sclass.domain.domains.teacher.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class TeacherPersonalInfo(
    @Column(length = 200)
    val address: String? = null,

    @Column(length = 20)
    val residentNumber: String? = null,

    @Column(length = 100)
    val bankAccount: String? = null,
)
