package com.sclass.domain.domains.teacher.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.teacher.exception.TeacherNotEditableException
import com.sclass.domain.domains.teacher.exception.TeacherNotSubmittableException
import com.sclass.domain.domains.teacher.exception.TeacherProfileIncompleteException
import com.sclass.domain.domains.teacher.exception.TeacherRequiredDocumentsMissingException
import com.sclass.domain.domains.user.domain.User
import com.sclass.domain.domains.user.domain.UserRoleState
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate
import java.time.LocalDateTime

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
    var profile: TeacherProfile? = null,

    @Embedded
    var education: TeacherEducation? = null,

    @Embedded
    var personalInfo: TeacherPersonalInfo? = null,

    @Embedded
    var contract: TeacherContract? = null,

    @Embedded
    var verification: TeacherVerification? = null,
) : BaseTimeEntity() {
    fun updateProfile(
        state: UserRoleState,
        birthDate: LocalDate,
        selfIntroduction: String?,
        majorCategory: MajorCategory,
        university: String,
        major: String,
        highSchool: String,
        address: String,
        residentNumber: String,
    ) {
        if (state != UserRoleState.DRAFT && state != UserRoleState.REJECTED && state != UserRoleState.PENDING) {
            throw TeacherNotEditableException()
        }
        profile = TeacherProfile(birthDate = birthDate, selfIntroduction = selfIntroduction)
        education =
            TeacherEducation(
                majorCategory = majorCategory,
                university = university,
                major = major,
                highSchool = highSchool,
            )
        personalInfo =
            TeacherPersonalInfo(
                address = address,
                residentNumber = residentNumber,
                bankAccount = personalInfo?.bankAccount,
            )
    }

    fun recordSubmission(
        documents: List<TeacherDocument>,
        state: UserRoleState,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        if (state != UserRoleState.DRAFT && state != UserRoleState.REJECTED && state != UserRoleState.PENDING) {
            throw TeacherNotSubmittableException()
        }
        validateProfileComplete()
        validateRequiredDocuments(documents)
        verification = TeacherVerification(submittedAt = now)
    }

    fun updateTeacherProfile(profile: TeacherProfile) {
        this.profile = profile
    }

    fun updateEducation(education: TeacherEducation) {
        this.education = education
    }

    fun updatePersonalInfo(personalInfo: TeacherPersonalInfo) {
        this.personalInfo = personalInfo
    }

    fun updateContract(contract: TeacherContract) {
        this.contract = contract
    }

    private fun validateProfileComplete() {
        if (profile?.birthDate == null ||
            education?.majorCategory == null ||
            education?.university.isNullOrBlank() ||
            education?.major.isNullOrBlank() ||
            education?.highSchool.isNullOrBlank() ||
            personalInfo?.address.isNullOrBlank() ||
            personalInfo?.residentNumber.isNullOrBlank()
        ) {
            throw TeacherProfileIncompleteException()
        }
    }

    private fun validateRequiredDocuments(documents: List<TeacherDocument>) {
        val uploadedTypes = documents.map { it.documentType }.toSet()
        if (!uploadedTypes.containsAll(REQUIRED_DOCUMENT_TYPES)) {
            throw TeacherRequiredDocumentsMissingException()
        }
    }

    companion object {
        val REQUIRED_DOCUMENT_TYPES =
            setOf(
                TeacherDocumentType.COMPLETION_CERTIFICATE,
                TeacherDocumentType.STUDENT_RECORD,
                TeacherDocumentType.RESIDENT_CERTIFICATE,
            )
    }
}
