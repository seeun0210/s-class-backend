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
        val updatedEducation =
            (teacher.education ?: TeacherEducation()).copy(
                majorCategory = education.majorCategory ?: teacher.education?.majorCategory,
                university = education.university ?: teacher.education?.university,
                major = education.major ?: teacher.education?.major,
                highSchool = education.highSchool ?: teacher.education?.highSchool,
            )
        teacher.updateEducation(updatedEducation)
        teacherAdaptor.save(teacher)
    }
}
