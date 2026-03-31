package com.sclass.backoffice.supportticket.controller

import com.sclass.backoffice.supportticket.dto.SupportTicketDetailResponse
import com.sclass.backoffice.supportticket.dto.SupportTicketListResponse
import com.sclass.backoffice.supportticket.usecase.GetOpenSupportTicketsUseCase
import com.sclass.backoffice.supportticket.usecase.ResolveSupportTicketUseCase
import com.sclass.common.dto.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/support-tickets")
class SupportTicketManagementController(
    private val getOpenSupportTicketsUseCase: GetOpenSupportTicketsUseCase,
    private val resolveSupportTicketUseCase: ResolveSupportTicketUseCase,
) {
    @GetMapping
    fun getOpenTickets(): ApiResponse<SupportTicketListResponse> = ApiResponse.success(getOpenSupportTicketsUseCase.execute())

    @PatchMapping("/{ticketId}/resolve")
    fun resolveTicket(
        @PathVariable ticketId: Long,
    ): ApiResponse<SupportTicketDetailResponse> = ApiResponse.success(resolveSupportTicketUseCase.execute(ticketId))
}
