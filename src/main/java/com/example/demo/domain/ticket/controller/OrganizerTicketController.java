package com.example.demo.domain.ticket.controller;

import com.example.demo.domain.ticket.dto.response.TicketResponseDTO;
import com.example.demo.domain.ticket.service.OrganizerTicketService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.global.response.status.SuccessStatus._OK;
@Slf4j
@RestController
@RequestMapping("/api/organizer/tickets")
@RequiredArgsConstructor
public class OrganizerTicketController {

    private final OrganizerTicketService organizerTicketService;

    @Operation(summary = "공연 입장")
    @PatchMapping("/{ticketId}/enter")
    public ApiResponse<?> enterTicket(@PathVariable("ticketId") Long ticketId) {
        log.info("PATCH /api/organizer/tickets/{}/enter", ticketId);
        organizerTicketService.enterTicket(ticketId);

        return ApiResponse.onSuccess(_OK, null);
    }

    @Operation(summary = "티켓 번호로 검증하기")
    @GetMapping("")
    public ApiResponse<TicketResponseDTO> validateTicketWithoutProof(
            @RequestParam("ticketNumber") String ticketNumber
    ) {
        log.info("GET /api/organizer/tickets?ticketNumber={}", ticketNumber);
        return ApiResponse.onSuccess(_OK, organizerTicketService.validateTicketWithoutProof(ticketNumber));
    }
}
