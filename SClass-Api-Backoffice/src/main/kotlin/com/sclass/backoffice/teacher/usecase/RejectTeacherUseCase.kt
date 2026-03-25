package com.sclass.backoffice.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class RejectTeacherUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDomainService: TeacherDomainService,
) {
    @Transactional
    fun execute(
        teacherId: String,
        reason: String,
    ) {
        val teacher = teacherAdaptor.findById(teacherId)
        teacherDomainService.reject(teacher, reason)
    }
}
