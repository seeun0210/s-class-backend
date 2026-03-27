package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.UpdateVerificationStatusRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateTeacherVerificationUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDomainService: TeacherDomainService,
) {
    @Transactional
    fun execute(
        teacherId: String,
        request: UpdateVerificationStatusRequest,
        userId: String,
    ) {
        val teacher = teacherAdaptor.findById(teacherId)
        if (request.isApproved) {
            teacherDomainService.approve(teacher, userId)
        } else {
            teacherDomainService.reject(teacher, request.requireReason)
        }
    }
}
