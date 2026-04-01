package com.sclass.backoffice.teacherassignment.usecase

import com.sclass.backoffice.teacherassignment.dto.AssignTeacherRequest
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacherassignment.service.TeacherAssignmentDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class AssignTeacherUseCase(
    private val teacherAssignmentDomainService: TeacherAssignmentDomainService,
    private val studentAdaptor: StudentAdaptor,
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional
    fun execute(
        request: AssignTeacherRequest,
        assignedBy: String,
    ) {
        studentAdaptor.findByUserId(request.studentUserId)
        teacherAdaptor.findByUserId(request.teacherUserId)

        teacherAssignmentDomainService.assign(
            studentUserId = request.studentUserId,
            teacherUserId = request.teacherUserId,
            platform = request.platform,
            organizationId = request.organizationId,
            assignedBy = assignedBy,
        )
    }
}
