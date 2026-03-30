package com.sclass.supporters.commission.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.commission.adaptor.CommissionAdaptor
import com.sclass.domain.domains.commission.adaptor.CommissionFileAdaptor
import com.sclass.domain.domains.commission.domain.Commission
import com.sclass.domain.domains.commission.domain.CommissionFile
import com.sclass.domain.domains.commission.domain.GuideInfo
import com.sclass.domain.domains.file.adaptor.FileAdaptor
import com.sclass.domain.domains.teacherassignment.adaptor.TeacherAssignmentAdaptor
import com.sclass.domain.domains.user.domain.Platform
import com.sclass.supporters.commission.dto.CommissionResponse
import com.sclass.supporters.commission.dto.CreateCommissionRequest
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateCommissionUseCase(
    private val commissionAdaptor: CommissionAdaptor,
    private val commissionFileAdaptor: CommissionFileAdaptor,
    private val teacherAssignmentAdaptor: TeacherAssignmentAdaptor,
    private val fileAdaptor: FileAdaptor,
) {
    @Transactional
    fun execute(
        studentUserId: String,
        request: CreateCommissionRequest,
    ): CommissionResponse {
        teacherAssignmentAdaptor.findActiveByStudentUserIdAndPlatformAndOrganizationId(
            studentUserId = studentUserId,
            platform = Platform.SUPPORTERS,
            organizationId = null,
        )

        val commission =
            commissionAdaptor.save(
                Commission(
                    studentUserId = studentUserId,
                    teacherUserId = request.teacherUserId,
                    outputFormat = request.outputFormat,
                    activityType = request.activityType,
                    guideInfo =
                        GuideInfo(
                            subject = request.guideInfo.subject,
                            volume = request.guideInfo.volume,
                            requiredElements = request.guideInfo.requiredElements,
                            gradingCriteria = request.guideInfo.gradingCriteria,
                            teacherEmphasis = request.guideInfo.teacherEmphasis,
                        ),
                ),
            )

        request.fileIds?.let { fileIds ->
            val files = fileIds.map { fileAdaptor.findById(it) }
            commissionFileAdaptor.saveAll(
                files.map { CommissionFile(commission = commission, file = it) },
            )
        }

        return CommissionResponse.from(commission)
    }
}
