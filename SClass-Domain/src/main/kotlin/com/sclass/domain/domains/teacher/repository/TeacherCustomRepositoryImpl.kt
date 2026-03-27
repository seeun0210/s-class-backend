package com.sclass.domain.domains.teacher.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sclass.domain.domains.teacher.domain.MajorCategory
import com.sclass.domain.domains.teacher.domain.QTeacher.teacher
import com.sclass.domain.domains.teacher.domain.TeacherVerificationStatus
import com.sclass.domain.domains.teacher.dto.TeacherSearchCondition
import com.sclass.domain.domains.teacher.dto.TeacherWithPlatform
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.domain.domains.user.domain.QUser.user
import com.sclass.domain.domains.user.domain.QUserRole.userRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

class TeacherCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : TeacherCustomRepository {
    override fun searchTeachers(
        condition: TeacherSearchCondition,
        page: Pageable,
    ): Page<TeacherWithPlatform> {
        val content =
            queryFactory
                .select(teacher, userRole.platform)
                .from(teacher)
                .join(teacher.user, user)
                .join(userRole)
                .on(user.id.eq(userRole.userId))
                .where(
                    nameContains(condition.name),
                    emailContains(condition.email),
                    universityContains(condition.university),
                    majorContains(condition.major),
                    majorCategoryEq(condition.majorCategory),
                    verificationStatusEq(condition.verificationStatus),
                    platformEq(condition.platform),
                    submittedAtGoe(condition.submittedAtFrom),
                    submittedAtLoe(condition.submittedAtTo),
                    createdAtGoe(condition.createdAtFrom),
                    createdAtLoe(condition.createdAtTo),
                ).offset(page.offset)
                .limit(page.pageSize.toLong())
                .orderBy(teacher.createdAt.desc())
                .fetch()
                .map { tuple ->
                    TeacherWithPlatform(
                        teacher = tuple.get(teacher) ?: error("Teacher must not be null"),
                        platform = tuple.get(userRole.platform) ?: error("Platform must not be null"),
                    )
                }

        val total =
            queryFactory
                .select(teacher.count())
                .from(teacher)
                .join(teacher.user, user)
                .where(
                    nameContains(condition.name),
                    emailContains(condition.email),
                    universityContains(condition.university),
                    majorContains(condition.major),
                    majorCategoryEq(condition.majorCategory),
                    verificationStatusEq(condition.verificationStatus),
                    submittedAtGoe(condition.submittedAtFrom),
                    submittedAtLoe(condition.submittedAtTo),
                    createdAtGoe(condition.createdAtFrom),
                    createdAtLoe(condition.createdAtTo),
                ).fetchOne() ?: 0L

        return PageImpl(content, page, total)
    }

    private fun nameContains(name: String?): BooleanExpression? = name?.let { user.name.contains(it) }

    private fun emailContains(email: String?): BooleanExpression? = email?.let { user.email.contains(it) }

    private fun universityContains(university: String?): BooleanExpression? = university?.let { teacher.education.university.contains(it) }

    private fun majorContains(major: String?): BooleanExpression? = major?.let { teacher.education.major.contains(it) }

    private fun majorCategoryEq(majorCategory: MajorCategory?): BooleanExpression? =
        majorCategory?.let { teacher.education.majorCategory.eq(it) }

    private fun verificationStatusEq(status: TeacherVerificationStatus?): BooleanExpression? =
        status?.let { teacher.verification.verificationStatus.eq(it) }

    private fun platformEq(platform: Platform?): BooleanExpression? = platform?.let { userRole.platform.eq(it) }

    private fun submittedAtGoe(from: LocalDateTime?): BooleanExpression? = from?.let { teacher.verification.submittedAt.goe(it) }

    private fun submittedAtLoe(to: LocalDateTime?): BooleanExpression? = to?.let { teacher.verification.submittedAt.loe(it) }

    private fun createdAtGoe(from: LocalDateTime?): BooleanExpression? = from?.let { teacher.createdAt.goe(it) }

    private fun createdAtLoe(to: LocalDateTime?): BooleanExpression? = to?.let { teacher.createdAt.loe(it) }
}
