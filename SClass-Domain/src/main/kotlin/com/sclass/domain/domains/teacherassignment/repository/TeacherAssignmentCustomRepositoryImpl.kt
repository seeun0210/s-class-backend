package com.sclass.domain.domains.teacherassignment.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.organization.domain.QOrganization.organization
import com.sclass.domain.domains.student.domain.QStudent.student
import com.sclass.domain.domains.teacher.domain.QTeacher.teacher
import com.sclass.domain.domains.teacherassignment.domain.QTeacherAssignment.teacherAssignment
import com.sclass.domain.domains.teacherassignment.dto.AssignedStudentInfo
import com.sclass.domain.domains.teacherassignment.dto.AssignedTeacherInfo

class TeacherAssignmentCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : TeacherAssignmentCustomRepository {
    override fun findActiveAssignedStudentsByTeacherId(teacherId: String): List<AssignedStudentInfo> =
        queryFactory
            .select(
                Projections.constructor(
                    AssignedStudentInfo::class.java,
                    teacherAssignment.id,
                    student.user.id,
                    student.user.name,
                    student.grade,
                    student.school,
                    teacherAssignment.platform,
                    teacherAssignment.organizationId,
                    organization.name,
                    teacherAssignment.assignedAt,
                ),
            ).from(teacherAssignment)
            .join(student)
            .on(student.user.id.eq(teacherAssignment.studentId))
            .leftJoin(organization)
            .on(organization.id.eq(teacherAssignment.organizationId))
            .where(
                teacherAssignment.teacherId.eq(teacherId),
                teacherAssignment.unassignedAt.isNull,
            ).fetch()

    override fun findActiveAssignedTeachersByStudentId(studentId: String): List<AssignedTeacherInfo> =
        queryFactory
            .select(
                Projections.constructor(
                    AssignedTeacherInfo::class.java,
                    teacherAssignment.id,
                    teacher.user.id,
                    teacher.user.name,
                    teacherAssignment.platform,
                    teacherAssignment.organizationId,
                    organization.name,
                    teacherAssignment.assignedAt,
                ),
            ).from(teacherAssignment)
            .join(teacher)
            .on(teacher.user.id.eq(teacherAssignment.teacherId))
            .leftJoin(organization)
            .on(organization.id.eq(teacherAssignment.organizationId))
            .where(
                teacherAssignment.studentId.eq(studentId),
                teacherAssignment.unassignedAt.isNull,
            ).fetch()
}
