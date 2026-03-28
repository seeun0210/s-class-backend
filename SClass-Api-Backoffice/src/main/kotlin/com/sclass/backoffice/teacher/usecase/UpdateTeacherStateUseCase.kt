package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.UpdateTeacherStateRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateTeacherStateUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDomainService: TeacherDomainService,
) {
    @Transactional
    fun execute(
        targetUserId: String,
        request: UpdateTeacherStateRequest,
        userId: String,
    ) {
        val teacher = teacherAdaptor.findByUserId(targetUserId)
        if (request.isApproved) {
            teacherDomainService.approve(teacher, request.platform, userId)
        } else {
            teacherDomainService.reject(teacher, request.platform, request.requireReason)
        }
    }
}
