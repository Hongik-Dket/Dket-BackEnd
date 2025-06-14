package com.example.demo.domain.ticket.controller;

import com.example.demo.domain.ticket.dto.PriceWeiDTO;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "티켓 가격 확인")
    @GetMapping("buyer/{sessionId}")
    public ApiResponse<PriceWeiDTO> getPriceWei(@PathVariable("sessionId") Long sessionId) {
        return ApiResponse.onSuccess(_OK, ticketService.getPriceWei(sessionId));
    }
}
