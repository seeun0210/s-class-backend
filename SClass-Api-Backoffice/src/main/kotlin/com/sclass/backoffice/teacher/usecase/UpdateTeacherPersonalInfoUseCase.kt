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
        val updatedPersonalInfo =
            (teacher.personalInfo ?: TeacherPersonalInfo()).copy(
                address = personalInfo.address ?: teacher.personalInfo?.address,
                residentNumber = personalInfo.residentNumber ?: teacher.personalInfo?.residentNumber,
                bankAccount = personalInfo.bankAccount ?: teacher.personalInfo?.bankAccount,
            )
        teacher.updatePersonalInfo(updatedPersonalInfo)
        teacherAdaptor.save(teacher)
    }
}
