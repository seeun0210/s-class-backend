package com.sclass.backoffice.student.usecase

import com.sclass.backoffice.student.dto.StudentDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.organization.adaptor.OrganizationAttributionAdaptor
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetStudentDetailUseCase(
    private val studentAdaptor: StudentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
    private val organizationAttributionAdaptor: OrganizationAttributionAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(studentId: String): StudentDetailResponse {
        val student = studentAdaptor.findByIdWithUser(studentId)
        val roles = userRoleAdaptor.findAllByUserId(student.user.id)
        val documents = studentAdaptor.findDocumentsWithFileByStudentId(student.id)
        val organizations = studentAdaptor.findOrganizationsByUserId(student.user.id)
        val attribution = organizationAttributionAdaptor.findByStudentIdOrNull(student.id)
        val attributions = listOfNotNull(attribution)
        return StudentDetailResponse.from(student, roles, documents, organizations, attributions)
    }
}
