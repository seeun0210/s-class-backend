package com.sclass.domain.domains.teacher.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.user.domain.User
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "teachers",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id"])],
)
class Teacher(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Embedded
    var profile: TeacherProfile = TeacherProfile(),

    @Embedded
    var education: TeacherEducation = TeacherEducation(),

    @Embedded
    var personalInfo: TeacherPersonalInfo = TeacherPersonalInfo(),

    @Embedded
    var contract: TeacherContract = TeacherContract(),

    @Embedded
    var verification: TeacherVerification = TeacherVerification(),
) : BaseTimeEntity()
