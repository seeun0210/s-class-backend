package com.sclass.domain.domains.teacher.domain

import com.sclass.domain.common.model.BaseTimeEntity
import com.sclass.domain.common.vo.Ulid
import com.sclass.domain.domains.teacher.exception.TeacherNotEditableException
import com.sclass.domain.domains.teacher.exception.TeacherNotPendingException
import com.sclass.domain.domains.teacher.exception.TeacherNotSubmittableException
import com.sclass.domain.domains.teacher.exception.TeacherProfileIncompleteException
import com.sclass.domain.domains.teacher.exception.TeacherRequiredDocumentsMissingException
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
    var profile: TeacherProfile = TeacherProfile(),

    @Embedded
    var education: TeacherEducation = TeacherEducation(),

    @Embedded
    var personalInfo: TeacherPersonalInfo = TeacherPersonalInfo(),

    @Embedded
    var contract: TeacherContract = TeacherContract(),

    @Embedded
    var verification: TeacherVerification = TeacherVerification(),
) : BaseTimeEntity() {
    fun updateProfile(
        birthDate: LocalDate,
        selfIntroduction: String?,
        majorCategory: MajorCategory,
        university: String,
        major: String,
        highSchool: String,
        address: String,
        residentNumber: String,
    ) {
        val status = verification.verificationStatus
        if (status != TeacherVerificationStatus.DRAFT && status != TeacherVerificationStatus.REJECTED) {
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
                bankAccount = personalInfo.bankAccount,
            )
    }

    fun submitForVerification(
        documents: List<TeacherDocument>,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        val status = verification.verificationStatus
        if (status != TeacherVerificationStatus.DRAFT && status != TeacherVerificationStatus.REJECTED) {
            throw TeacherNotSubmittableException()
        }
        validateProfileComplete()
        validateRequiredDocuments(documents)
        verification =
            TeacherVerification(
                verificationStatus = TeacherVerificationStatus.PENDING,
                submittedAt = now,
            )
    }

    fun approve(
        approvedBy: String,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        if (verification.verificationStatus != TeacherVerificationStatus.PENDING) {
            throw TeacherNotPendingException()
        }
        verification =
            TeacherVerification(
                verificationStatus = TeacherVerificationStatus.APPROVED,
                submittedAt = verification.submittedAt,
                approvedAt = now,
                approvedBy = approvedBy,
            )
    }

    fun reject(reason: String) {
        if (verification.verificationStatus != TeacherVerificationStatus.PENDING) {
            throw TeacherNotPendingException()
        }
        verification =
            TeacherVerification(
                verificationStatus = TeacherVerificationStatus.REJECTED,
                submittedAt = verification.submittedAt,
                rejectionReason = reason,
            )
    }

    private fun validateProfileComplete() {
        if (profile.birthDate == null ||
            education.majorCategory == null ||
            education.university.isNullOrBlank() ||
            education.major.isNullOrBlank() ||
            education.highSchool.isNullOrBlank() ||
            personalInfo.address.isNullOrBlank() ||
            personalInfo.residentNumber.isNullOrBlank()
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
