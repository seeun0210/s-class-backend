package com.sclass.supporters.student.dto

import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform

data class StudentProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val grade: Grade?,
    val school: String?,
    val parentPhoneNumber: String?,
    val platforms: List<Platform>,
    val documents: List<StudentDocumentResponse>,
) {
    companion object {
        fun from(
            student: Student,
            platforms: List<Platform>,
            documents: List<StudentDocumentResponse>,
        ): StudentProfileResponse =
            StudentProfileResponse(
                id = student.id,
                name = student.user.name,
                email = student.user.email,
                grade = student.grade,
                school = student.school,
                parentPhoneNumber = student.parentPhoneNumber,
                platforms = platforms,
                documents = documents,
            )
    }
}
