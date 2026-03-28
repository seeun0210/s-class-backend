package com.sclass.backoffice.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.TeacherPersonalInfo
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateTeacherPersonalInfoUseCase(
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        personalInfo: TeacherPersonalInfo,
    ) {
        val teacher = teacherAdaptor.findByUserId(userId)
        teacher.updatePersonalInfo(personalInfo)
        teacherAdaptor.save(teacher)
    }
}
