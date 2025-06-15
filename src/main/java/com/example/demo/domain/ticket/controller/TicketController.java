package com.example.demo.domain.ticket.controller;

import com.example.demo.domain.ticket.dto.PriceWeiDTO;
import com.example.demo.domain.ticket.dto.TicketDetailDTO;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "티켓 조회")
    @GetMapping("")
    public ApiResponse<TicketDetailDTO> getTicket(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String number
    ) {
        if ((id == null && number == null) || (id != null && number != null))
            throw new CustomException(ErrorStatus.TICKET_WRONG_PARAMETER);

        if (id != null) {
            return ApiResponse.onSuccess(_OK, ticketService.getTicketById(id));
        } else {
            return ApiResponse.onSuccess(_OK, ticketService.getTicketByNumber(number));
        }
    }

    @Operation(summary = "공연 입장")
    @PatchMapping("organizer/{ticketId}/enter")
    public ApiResponse<?> enterTicket(@PathVariable("ticketId") Long ticketId) {
        ticketService.enterTicket(ticketId);

        return ApiResponse.onSuccess(_OK, null);
    }

    @Operation(summary = "티켓 가격 확인")
    @GetMapping("buyer/{sessionId}")
    public ApiResponse<PriceWeiDTO> getPriceWei(@PathVariable("sessionId") Long sessionId) {
        return ApiResponse.onSuccess(_OK, ticketService.getPriceWei(sessionId));
    }
}
