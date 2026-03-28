package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.UpdateStudentProfileRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.user.domain.User
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateStudentProfileUseCase(
    private val studentAdaptor: StudentAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        request: UpdateStudentProfileRequest,
    ) {
        val student = studentAdaptor.findByUserId(userId)
        student.grade = request.grade ?: student.grade
        student.school = request.school ?: student.school
        student.parentPhoneNumber = request.parentPhoneNumber?.let { User.formatPhoneNumber(it) } ?: student.parentPhoneNumber
        studentAdaptor.save(student)
    }
}
