package com.sclass.backoffice.teacherassignment.usecase

import com.sclass.backoffice.teacherassignment.dto.AssignTeacherRequest
import com.sclass.backoffice.teacherassignment.dto.TeacherAssignmentResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.adaptor.OrganizationAdaptor
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacherassignment.service.TeacherAssignmentDomainService
import org.springframework.transaction.annotation.Transactional

@UseCase
class AssignTeacherUseCase(
    private val teacherAssignService: TeacherAssignmentDomainService,
    private val studentAdaptor: StudentAdaptor,
    private val teacherAdaptor: TeacherAdaptor,
    private val organizationAdaptor: OrganizationAdaptor,
) {
    @Transactional
    fun execute(
        request: AssignTeacherRequest,
        assignedBy: String,
    ): TeacherAssignmentResponse {
        studentAdaptor.findByUserId(request.studentId)
        val teacher =
            teacherAdaptor.findByUserIdWithUser(request.teacherId)

        val organizationName =
            request.organizationId?.let {
                organizationAdaptor.findById(it).name
            }

        val assignment =
            teacherAssignService.assign(
                studentId = request.studentId,
                teacherId = request.teacherId,
                platform = request.platform,
                organizationId = request.organizationId,
                assignedBy = assignedBy,
            )

        return TeacherAssignmentResponse.from(
            assignment = assignment,
            teacherName = teacher.user.name,
            organizationName = organizationName,
        )
    }
}
