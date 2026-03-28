package com.sclass.domain.domains.teacherassignment.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.organization.domain.QOrganization.organization
import com.sclass.domain.domains.student.domain.QStudent.student
import com.sclass.domain.domains.teacher.domain.QTeacher.teacher
import com.sclass.domain.domains.teacherassignment.domain.QTeacherAssignment.teacherAssignment
import com.sclass.domain.domains.teacherassignment.dto.AssignedStudentInfo
import com.sclass.domain.domains.teacherassignment.dto.AssignedTeacherInfo
import com.sclass.domain.domains.user.domain.QUser.user

class TeacherAssignmentCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : TeacherAssignmentCustomRepository {
    override fun findActiveAssignedStudentsByTeacherId(teacherId: String): List<AssignedStudentInfo> {
        val studentUser = user

        return queryFactory
            .select(
                Projections.constructor(
                    AssignedStudentInfo::class.java,
                    teacherAssignment.id,
                    studentUser.id,
                    studentUser.name,
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
            .join(studentUser)
            .on(studentUser.id.eq(teacherAssignment.studentId))
            .leftJoin(organization)
            .on(organization.id.eq(teacherAssignment.organizationId))
            .where(
                teacherAssignment.teacherId.eq(teacherId),
                teacherAssignment.unassignedAt.isNull,
            ).fetch()
    }

    override fun findActiveAssignedTeachersByStudentId(studentId: String): List<AssignedTeacherInfo> {
        val teacherUser = user

        return queryFactory
            .select(
                Projections.constructor(
                    AssignedTeacherInfo::class.java,
                    teacherAssignment.id,
                    teacherUser.id,
                    teacherUser.name,
                    teacherAssignment.platform,
                    teacherAssignment.organizationId,
                    organization.name,
                    teacherAssignment.assignedAt,
                ),
            ).from(teacherAssignment)
            .join(teacher)
            .on(teacher.user.id.eq(teacherAssignment.teacherId))
            .join(teacherUser)
            .on(teacherUser.id.eq(teacherAssignment.teacherId))
            .leftJoin(organization)
            .on(organization.id.eq(teacherAssignment.organizationId))
            .where(
                teacherAssignment.studentId.eq(studentId),
                teacherAssignment.unassignedAt.isNull,
            ).fetch()
    }
}
