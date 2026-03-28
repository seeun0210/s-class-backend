package com.sclass.backoffice.teacherassignment.usecase

import com.sclass.backoffice.teacherassignment.dto.UnassignTeacherRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacherassignment.service.TeacherAssignmentDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class UnassignTeacherUseCase(
    private val teacherAssignmentDomainService: TeacherAssignmentDomainService,
) {
    @Transactional
    fun execute(request: UnassignTeacherRequest) {
        teacherAssignmentDomainService.unassign(
            studentId = request.studentId,
            platform = request.platform,
            organizationId = request.organizationId,
        )
    }
}
