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
import com.sclass.domain.domains.student.domain.StudentDocumentType
import com.sclass.domain.domains.student.dto.StudentSearchCondition
import com.sclass.domain.domains.student.dto.StudentWithRoles
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
    ): Page<StudentWithRoles> {
        // Step 1: DISTINCT student IDs with filters + pagination
        val studentIds =
            queryFactory
                .select(student.id)
                .distinct()
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
                ).orderBy(student.createdAt.desc())
                .offset(page.offset)
                .limit(page.pageSize.toLong())
                .fetch()

        // Step 2: Batch load Students with User
        val students =
            if (studentIds.isEmpty()) {
                emptyList()
            } else {
                queryFactory
                    .selectFrom(student)
                    .join(student.user, user)
                    .fetchJoin()
                    .where(student.id.`in`(studentIds))
                    .orderBy(student.createdAt.desc())
                    .fetch()
            }

        // Step 3: Batch load UserRoles by userIds (STUDENT role only)
        val userIds = students.map { it.user.id }
        val rolesByUserId =
            if (userIds.isEmpty()) {
                emptyMap()
            } else {
                queryFactory
                    .selectFrom(userRole)
                    .where(
                        userRole.userId.`in`(userIds),
                        userRole.role.eq(Role.STUDENT),
                    ).fetch()
                    .groupBy { it.userId }
            }

        // Step 4: Combine into StudentWithRoles
        val content =
            students.map { s ->
                StudentWithRoles(
                    student = s,
                    roles = rolesByUserId[s.user.id] ?: emptyList(),
                )
            }

        // Count query: COUNT(DISTINCT student.id)
        val total =
            queryFactory
                .select(student.id.countDistinct())
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

    override fun findByUserIdWithUser(userId: String): Student? =
        queryFactory
            .selectFrom(student)
            .join(student.user, user)
            .fetchJoin()
            .where(user.id.eq(userId))
            .fetchOne()

    override fun findDocumentsWithFileByStudentId(studentId: String): List<StudentDocument> =
        queryFactory
            .selectFrom(studentDocument)
            .join(studentDocument.file, file)
            .fetchJoin()
            .where(studentDocument.student.id.eq(studentId))
            .fetch()

    override fun findAcademicDocumentsWithFileByUserIds(userIds: List<String>): Map<String, List<StudentDocument>> {
        if (userIds.isEmpty()) return emptyMap()

        val documents =
            queryFactory
                .selectFrom(studentDocument)
                .join(studentDocument.student, student)
                .fetchJoin()
                .join(studentDocument.file, file)
                .fetchJoin()
                .where(
                    student.user.id.`in`(userIds),
                    studentDocument.documentType.ne(StudentDocumentType.REGISTRATION_RECEIPT),
                ).fetch()

        return documents.groupBy { it.student.user.id }
    }

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
