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
        val updatedProfile =
            (teacher.profile ?: TeacherProfile()).copy(
                birthDate = profile.birthDate ?: teacher.profile?.birthDate,
                selfIntroduction = profile.selfIntroduction ?: teacher.profile?.selfIntroduction,
            )
        teacher.updateTeacherProfile(updatedProfile)
        teacherAdaptor.save(teacher)
    }
}
