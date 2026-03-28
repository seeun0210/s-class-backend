package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.StudentDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.adaptor.OrganizationAttributionAdaptor
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetStudentDetailUseCase(
    private val studentAdaptor: StudentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
    private val organizationAttributionAdaptor: OrganizationAttributionAdaptor,
    private val teacherAssignmentAdaptor: TeacherAssignmentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(userId: String): StudentDetailResponse {
        val student = studentAdaptor.findByUserIdWithUser(userId)
        val roles = userRoleAdaptor.findAllByUserId(userId)
        val documents = studentAdaptor.findDocumentsWithFileByStudentId(student.id)
        val organizations = studentAdaptor.findOrganizationsByUserId(userId)
        val attribution = organizationAttributionAdaptor.findByStudentIdOrNull(student.id)
        val attributions = listOfNotNull(attribution)
        val assignments = teacherAssignmentAdaptor.findActiveAssignedTeachersByStudentUserId(userId)
        return StudentDetailResponse.from(student, roles, documents, organizations, attributions, assignments)
    }
}
