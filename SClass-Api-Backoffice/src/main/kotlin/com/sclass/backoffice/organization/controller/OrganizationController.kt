package com.sclass.backoffice.organization.controller

import com.sclass.backoffice.organization.dto.CreateOrganizationRequest
import com.sclass.backoffice.organization.dto.CreateOrganizationUserRequest
import com.sclass.backoffice.organization.dto.OrganizationPageResponse
import com.sclass.backoffice.organization.dto.OrganizationResponse
import com.sclass.backoffice.organization.dto.OrganizationUserCreateResponse
import com.sclass.backoffice.organization.dto.OrganizationUserPageResponse
import com.sclass.backoffice.organization.dto.OrganizationUserStatsResponse
import com.sclass.backoffice.organization.dto.UpdateOrganizationSettingsRequest
import com.sclass.backoffice.organization.usecase.AddOrganizationUserUseCase
import com.sclass.backoffice.organization.usecase.CreateOrganizationUseCase
import com.sclass.backoffice.organization.usecase.CreateOrganizationUserUseCase
import com.sclass.backoffice.organization.usecase.GetOrganizationStatsUseCase
import com.sclass.backoffice.organization.usecase.GetOrganizationUseCase
import com.sclass.backoffice.organization.usecase.GetOrganizationUsersUseCase
import com.sclass.backoffice.organization.usecase.GetOrganizationsUseCase
import com.sclass.backoffice.organization.usecase.UpdateOrganizationSettingsUseCase
import com.sclass.common.dto.ApiResponse
import com.sclass.common.dto.ApiResponse.Companion.success
import com.sclass.domain.domains.organization.dto.OrganizationUserSearchCondition
import com.sclass.domain.domains.user.domain.Role
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/organizations")
class OrganizationController(
    private val getOrganizationsUseCase: GetOrganizationsUseCase,
    private val getOrganizationUseCase: GetOrganizationUseCase,
    private val createOrganizationUseCase: CreateOrganizationUseCase,
    private val updateOrganizationSettingsUseCase: UpdateOrganizationSettingsUseCase,
    private val getOrganizationUsersUseCase: GetOrganizationUsersUseCase,
    private val getOrganizationStatsUseCase: GetOrganizationStatsUseCase,
    private val createOrganizationUserUseCase: CreateOrganizationUserUseCase,
    private val addOrganizationUserUseCase: AddOrganizationUserUseCase,
) {
    @GetMapping
    fun getOrganizations(
        @PageableDefault(size = 20, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ApiResponse<OrganizationPageResponse> = success(getOrganizationsUseCase.execute(pageable))

    @GetMapping("/{organizationId}")
    fun getOrganization(
        @PathVariable organizationId: Long,
    ): ApiResponse<OrganizationResponse> = success(getOrganizationUseCase.execute(organizationId))

    @PostMapping
    fun createOrganization(
        @Valid @RequestBody request: CreateOrganizationRequest,
    ): ApiResponse<OrganizationResponse> = success(createOrganizationUseCase.execute(request))

    @PutMapping("/{organizationId}/settings")
    fun updateSettings(
        @PathVariable organizationId: Long,
        @Valid @RequestBody request: UpdateOrganizationSettingsRequest,
    ): ApiResponse<OrganizationResponse> = success(updateOrganizationSettingsUseCase.execute(organizationId, request))

    @GetMapping("/{organizationId}/users")
    fun getOrganizationUsers(
        @PathVariable organizationId: Long,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) role: Role?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ApiResponse<OrganizationUserPageResponse> =
        success(
            getOrganizationUsersUseCase.execute(
                organizationId,
                OrganizationUserSearchCondition(name = name, email = email, role = role),
                pageable,
            ),
        )

    @GetMapping("/{organizationId}/stats")
    fun getOrganizationStats(
        @PathVariable organizationId: Long,
    ): ApiResponse<OrganizationUserStatsResponse> = success(getOrganizationStatsUseCase.execute(organizationId))

    @PutMapping("/{organizationId}/users/{userId}")
    fun addOrganizationUser(
        @PathVariable organizationId: Long,
        @PathVariable userId: String,
    ): ApiResponse<OrganizationUserCreateResponse> = success(addOrganizationUserUseCase.execute(organizationId, userId))

    @PostMapping("/{organizationId}/users")
    fun createOrganizationUser(
        @PathVariable organizationId: Long,
        @Valid @RequestBody request: CreateOrganizationUserRequest,
    ): ApiResponse<OrganizationUserCreateResponse> = success(createOrganizationUserUseCase.execute(organizationId, request))
}
