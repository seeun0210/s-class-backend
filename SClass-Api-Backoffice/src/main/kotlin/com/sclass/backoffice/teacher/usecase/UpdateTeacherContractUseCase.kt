package com.sclass.backoffice.teacher.usecase

import com.sclass.common.annotation.UseCase
import com.sclass.domain.domains.teacher.adaptor.TeacherAdaptor
import com.sclass.domain.domains.teacher.domain.TeacherContract
import org.springframework.transaction.annotation.Transactional

@UseCase
class UpdateTeacherContractUseCase(
    private val teacherAdaptor: TeacherAdaptor,
) {
    @Transactional
    fun execute(
        userId: String,
        contract: TeacherContract,
    ) {
        val teacher = teacherAdaptor.findByUserId(userId)
        val updatedContract =
            (teacher.contract ?: TeacherContract()).copy(
                policeCheckAt = contract.policeCheckAt ?: teacher.contract?.policeCheckAt,
                contractStartDate = contract.contractStartDate ?: teacher.contract?.contractStartDate,
                contractEndDate = contract.contractEndDate ?: teacher.contract?.contractEndDate,
            )
        teacher.updateContract(updatedContract)
        teacherAdaptor.save(teacher)
    }
}
