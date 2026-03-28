package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.UpdateStudentProfileRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
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
        student.grade = request.grade
        student.school = request.school
        student.parentPhoneNumber = request.parentPhoneNumber
        studentAdaptor.save(student)
    }
}
