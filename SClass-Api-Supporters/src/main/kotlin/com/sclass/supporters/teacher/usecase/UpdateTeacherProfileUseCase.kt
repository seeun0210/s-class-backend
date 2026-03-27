package com.sclass.supporters.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.adaptor.TeacherDocumentAdaptor
import com.sclass.domain.domains.teacher.service.TeacherDomainService
import com.sclass.supporters.teacher.dto.TeacherDocumentResponse
import com.sclass.supporters.teacher.dto.TeacherProfileResponse
import com.sclass.supporters.teacher.dto.UpdateTeacherProfileRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateTeacherProfileUseCase(
    private val teacherAdaptor: TeacherAdaptor,
    private val teacherDomainService: TeacherDomainService,
    private val teacherDocumentAdaptor: TeacherDocumentAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        request: UpdateTeacherProfileRequest,
    ): TeacherProfileResponse {
        val teacher = teacherAdaptor.findByUserId(userId)
        val updated =
            teacherDomainService.updateProfile(
                teacher = teacher,
                birthDate = request.birthDate,
                selfIntroduction = request.selfIntroduction,
                majorCategory = request.majorCategory,
                university = request.university,
                major = request.major,
                highSchool = request.highSchool,
                address = request.address,
                residentNumber = request.residentNumber,
            )
        val documents = teacherDocumentAdaptor.findAllByTeacherId(updated.id)
        return TeacherProfileResponse.from(
            teacher = updated,
            documents = documents.map { TeacherDocumentResponse.from(it) },
        )
    }
}
