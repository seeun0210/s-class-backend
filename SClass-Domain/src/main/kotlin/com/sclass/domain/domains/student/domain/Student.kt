package com.sclass.domain.domains.student.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "students",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id"])],
)
class Student(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var grade: Grade? = null,

    @Column(length = 20)
    var parentPhoneNumber: String? = null,
) : BaseTimeEntity()
