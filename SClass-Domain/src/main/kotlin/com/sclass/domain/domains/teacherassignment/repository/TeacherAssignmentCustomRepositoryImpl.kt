package com.sclass.domain.domains.teacherassignment.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.organization.domain.QOrganization.organization
import com.sclass.domain.domains.student.domain.QStudent.student
import com.sclass.domain.domains.teacher.domain.QTeacher.teacher
import com.sclass.domain.domains.teacherassignment.domain.QTeacherAssignment.teacherAssignment
import com.sclass.domain.domains.teacherassignment.dto.AssignedStudentInfo
import com.sclass.domain.domains.teacherassignment.dto.AssignedTeacherInfo
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentListInfo
import com.sclass.domain.domains.teacherassignment.dto.TeacherAssignmentSearchCondition
import com.sclass.domain.domains.user.domain.Platform
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class TeacherAssignmentCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : TeacherAssignmentCustomRepository {
    override fun findActiveAssignedStudentsByTeacherUserId(teacherUserId: String): List<AssignedStudentInfo> =
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
            .on(student.user.id.eq(teacherAssignment.studentUserId))
            .leftJoin(organization)
            .on(organization.id.eq(teacherAssignment.organizationId))
            .where(
                teacherAssignment.teacherUserId.eq(teacherUserId),
                teacherAssignment.unassignedAt.isNull,
            ).fetch()

    override fun findActiveAssignedTeachersByStudentUserId(studentUserId: String): List<AssignedTeacherInfo> =
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
            .on(teacher.user.id.eq(teacherAssignment.teacherUserId))
            .leftJoin(organization)
            .on(organization.id.eq(teacherAssignment.organizationId))
            .where(
                teacherAssignment.studentUserId.eq(studentUserId),
                teacherAssignment.unassignedAt.isNull,
            ).fetch()

    override fun searchActiveAssignments(
        condition: TeacherAssignmentSearchCondition,
        pageable: Pageable,
    ): Page<TeacherAssignmentListInfo> {
        val content =
            queryFactory
                .select(
                    Projections.constructor(
                        TeacherAssignmentListInfo::class.java,
                        teacherAssignment.id,
                        student.user.id,
                        student.user.name,
                        teacher.user.id,
                        teacher.user.name,
                        teacherAssignment.platform,
                        teacherAssignment.organizationId,
                        organization.name,
                        teacherAssignment.assignedAt,
                    ),
                ).from(teacherAssignment)
                .join(student)
                .on(student.user.id.eq(teacherAssignment.studentUserId))
                .join(teacher)
                .on(teacher.user.id.eq(teacherAssignment.teacherUserId))
                .leftJoin(organization)
                .on(
                    organization.id.eq(
                        teacherAssignment
                            .organizationId,
                    ),
                ).where(
                    teacherAssignment.unassignedAt.isNull,
                    platformEq(condition.platform),
                    organizationIdEq(condition.organizationId),
                    teacherNameContains(condition.teacherName),
                    studentNameContains(condition.studentName),
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .orderBy(teacherAssignment.assignedAt.desc())
                .fetch()

        val total =
            queryFactory
                .select(teacherAssignment.count())
                .from(teacherAssignment)
                .join(student)
                .on(student.user.id.eq(teacherAssignment.studentUserId))
                .join(teacher)
                .on(teacher.user.id.eq(teacherAssignment.teacherUserId))
                .where(
                    teacherAssignment.unassignedAt.isNull,
                    platformEq(condition.platform),
                    organizationIdEq(condition.organizationId),
                    teacherNameContains(condition.teacherName),
                    studentNameContains(condition.studentName),
                ).fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun findActiveAssignedStudentsByTeacherUserId(
        teacherUserId: String,
        platform: Platform?,
    ): List<AssignedStudentInfo> =
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
            .on(student.user.id.eq(teacherAssignment.studentUserId))
            .leftJoin(organization)
            .on(organization.id.eq(teacherAssignment.organizationId))
            .where(
                teacherAssignment.teacherUserId.eq(teacherUserId),
                teacherAssignment.unassignedAt.isNull,
                platform?.let { teacherAssignment.platform.eq(it) },
            ).fetch()

    private fun platformEq(platform: Platform?) = platform?.let { teacherAssignment.platform.eq(it) }

    private fun organizationIdEq(organizationId: Long?) = organizationId?.let { teacherAssignment.organizationId.eq(it) }

    private fun teacherNameContains(teacherName: String?) = teacherName?.let { teacher.user.name.contains(it) }

    private fun studentNameContains(studentName: String?) = studentName?.let { student.user.name.contains(it) }
}
