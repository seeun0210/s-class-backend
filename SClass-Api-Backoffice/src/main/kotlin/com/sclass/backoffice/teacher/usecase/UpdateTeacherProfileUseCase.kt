package com.sclass.backoffice.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.TeacherProfile
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateTeacherProfileUseCase(
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        profile: TeacherProfile,
    ) {
        val teacher = teacherAdaptor.findByUserId(userId)
        teacher.updateTeacherProfile(profile)
        teacherAdaptor.save(teacher)
    }
}
