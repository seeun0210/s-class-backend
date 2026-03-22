package com.sclass.domain.domains.teacher.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "teachers",
    uniqueConstraints = [UniqueConstraint(columnNames = ["userId", "organizationId"])],
)
class Teacher(
    @Id
    @Column(length = 26)
    val id: String = Ulid.generate(),

    @Column(nullable = false, length = 26)
    val userId: String,

    @Column(nullable = true)
    val organizationId: Long? = null,

    @Embedded
    var profile: TeacherProfile = TeacherProfile(),

    @Embedded
    var education: TeacherEducation = TeacherEducation(),

    @Embedded
    var personalInfo: TeacherPersonalInfo = TeacherPersonalInfo(),

    @Embedded
    var documentFiles: TeacherDocumentFiles = TeacherDocumentFiles(),

    @Embedded
    var contract: TeacherContract = TeacherContract(),

    @Embedded
    var verification: TeacherVerification = TeacherVerification(),
) : BaseTimeEntity()
