package com.example.demo.domain.ticket.controller;

import com.example.demo.domain.ticket.dto.TicketDetailDTO;
import com.example.demo.domain.ticket.service.BuyerTicketService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@Slf4j
@RestController
@RequestMapping("/api/buyer/tickets")
@RequiredArgsConstructor
public class BuyerTicketController {

    private final BuyerTicketService buyerTicketService;

    @Operation(summary = "티켓 조회")
    @GetMapping("/{ticketId}")
    public ApiResponse<TicketDetailDTO> getTicket(
            @PathVariable("ticketId") Long ticketId
    ) {
        log.info("GET /api/buyer/tickets/{}", ticketId);
        return ApiResponse.onSuccess(_OK, buyerTicketService.getTicketDetail(ticketId));
    }

}
