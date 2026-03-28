package com.sclass.backoffice.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.TeacherEducation
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateTeacherEducationUseCase(
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        education: TeacherEducation,
    ) {
        val teacher = teacherAdaptor.findByUserId(userId)
        teacher.updateEducation(education)
        teacherAdaptor.save(teacher)
    }
}
