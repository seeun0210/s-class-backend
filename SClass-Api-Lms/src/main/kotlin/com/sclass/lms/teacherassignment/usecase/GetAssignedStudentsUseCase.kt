package com.sclass.lms.teacherassignment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.lms.teacherassignment.dto.AssignedStudentResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetAssignedStudentsUseCase(
    private val teacherAssignedAdaptor: TeacherAssignmentAdaptor,
    private val studentAdaptor: StudentAdaptor,
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(currentUserId: String): List<AssignedStudentResponse> {
        teacherAdaptor.findByUserId(currentUserId)

        val assignedStudents =
            teacherAssignedAdaptor.findActiveAssignedStudentsByTeacherUserId(currentUserId, Platform.LMS)

        if (assignedStudents.isEmpty()) return emptyList()

        val documentsByUserId =
            studentAdaptor.findDocumentsWithFileByUserIds(assignedStudents.map { it.studentUserId })

        return assignedStudents.map { info ->
            AssignedStudentResponse.from(info, documentsByUserId[info.studentUserId] ?: emptyList())
        }
    }
}
