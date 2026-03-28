package com.sclass.domain.domains.teacherassignment.repository

import com.sclass.domain.domains.teacherassignment.dto.AssignedStudentInfo
import com.sclass.domain.domains.teacherassignment.dto.AssignedTeacherInfo
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentListInfo
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentSearchCondition
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TeacherAssignmentCustomRepository {
    fun findActiveAssignedStudentsByTeacherId(teacherId: String): List<AssignedStudentInfo>

    fun findActiveAssignedTeachersByStudentId(studentId: String): List<AssignedTeacherInfo>

    fun searchActiveAssignments(
        condition: TeacherAssignmentSearchCondition,
        pageable: Pageable,
    ): Page<TeacherAssignmentListInfo>
}
