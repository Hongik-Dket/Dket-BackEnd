package com.example.demo.domain.ticket.controller;

import com.example.demo.domain.ticket.dto.request.EntryCodeDTO;
import com.example.demo.domain.ticket.dto.response.EntryProofDataDTO;
import com.example.demo.domain.ticket.dto.response.TicketDetailDTO;
import com.example.demo.domain.ticket.service.BuyerTicketService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

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
            return ApiResponse.onSuccess(_OK, buyerTicketService.getTicketDetail(ticketId));
    }

    @Operation(summary = "입장 인증 번호 확인")
    @PostMapping("/{ticketId}/enter/prepare")
    public ApiResponse<EntryProofDataDTO> getEntryProofData(
            @PathVariable("ticketId") Long ticketId,
            @RequestBody EntryCodeDTO request
    ) {
        return ApiResponse.onSuccess(_OK, buyerTicketService.getEntryProofData(ticketId, request));
    }
}
