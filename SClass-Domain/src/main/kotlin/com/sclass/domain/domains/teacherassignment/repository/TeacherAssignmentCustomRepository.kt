package com.sclass.domain.domains.teacherassignment.repository

import com.sclass.domain.domains.teacherassignment.dto.AssignedStudentInfo
import com.sclass.domain.domains.teacherassignment.dto.AssignedTeacherInfo
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentListInfo
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentSearchCondition
import com.sclass.domain.domains.user.domain.Platform
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TeacherAssignmentCustomRepository {
    fun findActiveAssignedStudentsByTeacherUserId(
        teacherUserId: String,
        platform: Platform? = null,
    ): List<AssignedStudentInfo>

    fun findActiveAssignedTeachersByStudentUserId(studentUserId: String): List<AssignedTeacherInfo>

    fun searchActiveAssignments(
        condition: TeacherAssignmentSearchCondition,
        pageable: Pageable,
    ): Page<TeacherAssignmentListInfo>
}
