package com.sclass.domain.domains.student.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.file.domain.QFile.file
import com.sclass.domain.domains.organization.domain.OrganizationUser
import com.sclass.domain.domains.organization.domain.QOrganization.organization
import com.sclass.domain.domains.organization.domain.QOrganizationUser.organizationUser
import com.sclass.domain.domains.student.domain.QStudent.student
import com.sclass.domain.domains.student.domain.QStudentDocument.studentDocument
import com.sclass.domain.domains.student.domain.Student
import com.sclass.domain.domains.student.domain.StudentDocument
import com.sclass.domain.domains.student.dto.StudentSearchCondition
import com.sclass.domain.domains.student.dto.StudentWithPlatform
import com.sclass.domain.domains.user.domain.Grade
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.QUser.user
import com.sclass.domain.domains.user.domain.QUserRole.userRole
import com.sclass.domain.domains.user.domain.Role
import com.sclass.domain.domains.user.domain.UserRoleState
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

class StudentCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : StudentCustomRepository {
    override fun searchStudents(
        condition: StudentSearchCondition,
        page: Pageable,
    ): Page<StudentWithPlatform> {
        val content =
            queryFactory
                .select(student, userRole.platform, userRole.state)
                .from(student)
                .join(student.user, user)
                .join(userRole)
                .on(user.id.eq(userRole.userId), userRole.role.eq(Role.STUDENT))
                .where(
                    nameContains(condition.name),
                    emailContains(condition.email),
                    gradeEq(condition.grade),
                    schoolContains(condition.school),
                    stateEq(condition.state),
                    platformEq(condition.platform),
                    createdAtGoe(condition.createdAtFrom),
                    createdAtLoe(condition.createdAtTo),
                ).offset(page.offset)
                .limit(page.pageSize.toLong())
                .orderBy(student.createdAt.desc())
                .fetch()
                .map { tuple ->
                    StudentWithPlatform(
                        student = tuple.get(student) ?: error("Student must not be null"),
                        platform = tuple.get(userRole.platform) ?: error("Platform must not be null"),
                        state = tuple.get(userRole.state) ?: error("State must not be null"),
                    )
                }

        val total =
            queryFactory
                .select(student.count())
                .from(student)
                .join(student.user, user)
                .join(userRole)
                .on(user.id.eq(userRole.userId), userRole.role.eq(Role.STUDENT))
                .where(
                    nameContains(condition.name),
                    emailContains(condition.email),
                    gradeEq(condition.grade),
                    schoolContains(condition.school),
                    stateEq(condition.state),
                    platformEq(condition.platform),
                    createdAtGoe(condition.createdAtFrom),
                    createdAtLoe(condition.createdAtTo),
                ).fetchOne() ?: 0L

        return PageImpl(content, page, total)
    }

    override fun findByIdWithUser(id: String): Student? =
        queryFactory
            .selectFrom(student)
            .join(student.user, user)
            .fetchJoin()
            .where(student.id.eq(id))
            .fetchOne()

    override fun findDocumentsWithFileByStudentId(studentId: String): List<StudentDocument> =
        queryFactory
            .selectFrom(studentDocument)
            .join(studentDocument.file, file)
            .fetchJoin()
            .where(studentDocument.student.id.eq(studentId))
            .fetch()

    override fun findOrganizationsByUserId(userId: String): List<OrganizationUser> =
        queryFactory
            .selectFrom(organizationUser)
            .join(organizationUser.organization, organization)
            .fetchJoin()
            .where(organizationUser.user.id.eq(userId))
            .fetch()

    private fun nameContains(name: String?): BooleanExpression? = name?.let { user.name.contains(it) }

    private fun emailContains(email: String?): BooleanExpression? = email?.let { user.email.contains(it) }

    private fun gradeEq(grade: Grade?): BooleanExpression? = grade?.let { student.grade.eq(it) }

    private fun schoolContains(school: String?): BooleanExpression? = school?.let { student.school.contains(it) }

    private fun stateEq(state: UserRoleState?): BooleanExpression? = state?.let { userRole.state.eq(it) }

    private fun platformEq(platform: Platform?): BooleanExpression? = platform?.let { userRole.platform.eq(it) }

    private fun createdAtGoe(from: LocalDateTime?): BooleanExpression? = from?.let { student.createdAt.goe(it) }

    private fun createdAtLoe(to: LocalDateTime?): BooleanExpression? = to?.let { student.createdAt.loe(it) }
}
