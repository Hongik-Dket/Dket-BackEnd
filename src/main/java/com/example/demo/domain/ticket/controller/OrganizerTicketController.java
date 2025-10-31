package com.example.demo.domain.ticket.controller;

import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/organizer/tickets")
@RequiredArgsConstructor
public class OrganizerTicketController {

    private final TicketService ticketService;

    @Operation(summary = "공연 입장")
    @PatchMapping("/{ticketId}/enter")
    public ApiResponse<?> enterTicket(@PathVariable("ticketId") Long ticketId) {
        ticketService.enterTicket(ticketId);

        return ApiResponse.onSuccess(_OK, null);
    }
}
