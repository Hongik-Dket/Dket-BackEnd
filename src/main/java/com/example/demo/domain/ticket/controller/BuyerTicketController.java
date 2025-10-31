package com.example.demo.domain.ticket.controller;

import com.example.demo.domain.ticket.dto.TicketDetailDTO;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/buyer/tickets")
@RequiredArgsConstructor
public class BuyerTicketController {

    private final TicketService ticketService;

    @Operation(summary = "티켓 조회")
    @GetMapping("/{ticketId}")
    public ApiResponse<TicketDetailDTO> getTicket(
            @PathVariable("ticketId") Long ticketId
    ) {
            return ApiResponse.onSuccess(_OK, ticketService.getTicketDetail(ticketId));
    }
}
