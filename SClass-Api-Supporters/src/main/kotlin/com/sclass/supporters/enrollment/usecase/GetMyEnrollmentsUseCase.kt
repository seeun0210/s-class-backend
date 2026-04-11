package com.sclass.supporters.enrollment.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.enrollment.adaptor.EnrollmentAdaptor
import com.sclass.supporters.enrollment.dto.MyEnrollmentResponse
import org.springframework.transaction.annotation.Transactional

@UseCase
class GetMyEnrollmentsUseCase(
    private val enrollmentAdaptor: EnrollmentAdaptor,
) {
    @Transactional(readOnly = true)
    fun execute(studentUserId: String): List<MyEnrollmentResponse> =
        enrollmentAdaptor.findAllByStudent(studentUserId).map { MyEnrollmentResponse.from(it) }
}
