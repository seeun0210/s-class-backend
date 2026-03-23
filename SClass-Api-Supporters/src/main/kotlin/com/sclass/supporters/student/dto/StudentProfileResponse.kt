package com.sclass.supporters.student.dto

import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.user.domain.Grade

data class StudentProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val grade: Grade?,
    val school: String?,
    val parentPhoneNumber: String?,
    val documents: List<StudentDocumentResponse>,
) {
    companion object {
        fun from(
            student: Student,
            documents: List<StudentDocumentResponse>,
        ): StudentProfileResponse =
            StudentProfileResponse(
                id = student.id,
                name = student.user.name,
                email = student.user.email,
                grade = student.grade,
                school = student.school,
                parentPhoneNumber = student.parentPhoneNumber,
                documents = documents,
            )
    }
}
