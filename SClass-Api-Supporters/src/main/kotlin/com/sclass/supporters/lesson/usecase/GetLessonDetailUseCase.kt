package com.sclass.supporters.lesson.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.inquiryplan.adaptor.InquiryPlanAdaptor
import com.sclass.domain.domains.inquiryplan.domain.InquiryPlanSourceType
import com.sclass.domain.domains.lesson.adaptor.LessonAdaptor
import com.sclass.domain.domains.lesson.exception.LessonUnauthorizedAccessException
import com.sclass.domain.domains.student.adaptor.StudentAdaptor
import com.sclass.domain.domains.student.adaptor.StudentDocumentAdaptor
import com.sclass.domain.domains.user.adaptor.UserRoleAdaptor
import com.sclass.domain.domains.user.domain.activePlatforms
import com.sclass.supporters.inquiry.dto.InquiryPlanResponse
import com.sclass.supporters.lesson.dto.LessonDetailResponse
import com.sclass.supporters.student.dto.StudentDocumentResponse
import com.sclass.supporters.student.dto.StudentProfileResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetLessonDetailUseCase(
    private val lessonAdaptor: LessonAdaptor,
    private val inquiryPlanAdaptor: InquiryPlanAdaptor,
    private val studentAdaptor: StudentAdaptor,
    private val studentDocumentAdaptor: StudentDocumentAdaptor,
    private val userRoleAdaptor: UserRoleAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: String,
        lessonId: Long,
    ): LessonDetailResponse {
        val lesson = lessonAdaptor.findById(lessonId)
        if (!lesson.isTeacher(userId)) {
            throw LessonUnauthorizedAccessException()
        }
        val student = studentAdaptor.findByUserIdWithUser(lesson.studentUserId)
        val documents = studentDocumentAdaptor.findAllByStudentId(student.id)
        val platforms = userRoleAdaptor.findAllByUserId(lesson.studentUserId).activePlatforms()
        val studentProfile =
            StudentProfileResponse.from(
                student = student,
                platforms = platforms,
                documents = documents.map { StudentDocumentResponse.from(it) },
            )
        val inquiryPlans =
            inquiryPlanAdaptor
                .findAllBySourceOrderByIdDesc(InquiryPlanSourceType.LESSON, lessonId)
                .map { InquiryPlanResponse.from(it) }
        return LessonDetailResponse.from(lesson, studentProfile, inquiryPlans)
    }
}
