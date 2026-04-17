package com.sclass.supporters.partnership.controller

import com.sclass.common.dto.ApiResponse
import com.sclass.supporters.partnership.dto.CreatePartnershipLeadRequest
import com.sclass.supporters.partnership.dto.PartnershipLeadResponse
import com.sclass.supporters.partnership.usecase.CreatePartnershipLeadUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/partnership-leads")
class PartnershipLeadController(
    private val createPartnershipLeadUseCase: CreatePartnershipLeadUseCase,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreatePartnershipLeadRequest,
    ): ApiResponse<PartnershipLeadResponse> = ApiResponse.success(createPartnershipLeadUseCase.execute(request))
}
