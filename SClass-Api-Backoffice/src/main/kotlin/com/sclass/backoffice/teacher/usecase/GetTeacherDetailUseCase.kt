package com.sclass.backoffice.teacher.usecase

import com.sclass.backoffice.teacher.dto.TeacherDetailResponse
import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetTeacherDetailUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(teacherId: String): TeacherDetailResponse {
        val teacher = teacherAdaptor.findByIdWithUser(teacherId)
        val roles = userRoleAdaptor.findAllByUserId(teacher.user.id)
        val documents = teacherAdaptor.findDocumentsWithFileByTeacherId(teacher.id)
        val organizations = teacherAdaptor.findOrganizationsByUserId(teacher.user.id)
        return TeacherDetailResponse.from(teacher, roles, documents, organizations)
    }
}
