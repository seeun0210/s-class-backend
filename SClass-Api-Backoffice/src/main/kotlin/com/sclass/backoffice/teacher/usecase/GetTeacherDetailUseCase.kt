package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.TeacherDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetTeacherDetailUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
    private val teacherAssignmentAdaptor: TeacherAssignmentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(userId: String): TeacherDetailResponse {
        val teacher = teacherAdaptor.findByUserIdWithUser(userId)
        val roles = userRoleAdaptor.findAllByUserId(userId)
        val documents = teacherAdaptor.findDocumentsWithFileByTeacherId(teacher.id)
        val organizations = teacherAdaptor.findOrganizationsByUserId(userId)
        val assignments = teacherAssignmentAdaptor.findActiveAssignedStudentsByTeacherUserId(userId)
        return TeacherDetailResponse.from(teacher, roles, documents, organizations, assignments)
    }
}
