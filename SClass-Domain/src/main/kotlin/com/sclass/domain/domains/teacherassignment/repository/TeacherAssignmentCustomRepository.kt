package com.sclass.domain.domains.teacherassignment.repository

import com.sclass.domain.domains.teacherassignment.dto.AssignedStudentInfo
import com.sclass.domain.domains.teacherassignment.dto.AssignedTeacherInfo

interface TeacherAssignmentCustomRepository {
    fun findActiveAssignedStudentsByTeacherId(teacherId: String): List<AssignedStudentInfo>

    fun findActiveAssignedTeachersByStudentId(studentId: String): List<AssignedTeacherInfo>
}
