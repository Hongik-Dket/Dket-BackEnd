package com.example.demo.domain.event.controller;

import com.example.demo.domain.event.dto.EventInfoDTO;
import com.example.demo.domain.event.dto.SessionInfoDTO;
import com.example.demo.domain.event.service.OrganizerEventService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/organizer/events")
@RequiredArgsConstructor
public class OrganizerEventController {

    private final OrganizerEventService organizerEventService;

    @Operation(summary = "개최자 - 공연 상세 조회")
    @GetMapping("{eventId}")
    public ApiResponse<EventInfoDTO> getEventInfoForOrganizer(@PathVariable Long eventId) {
        return ApiResponse.onSuccess(_OK, organizerEventService.getEventInfoForOrganizer(eventId));
    }

    @Operation(summary = "개최자 - 회차 상세 조회")
    @GetMapping("{eventId}/{sessionId}")
    public ApiResponse<SessionInfoDTO> getSessionInfoForOrganizer(
            @PathVariable Long eventId, @PathVariable Long sessionId) {
        return ApiResponse.onSuccess(_OK, organizerEventService.getSessionInfoForOrganizer(eventId, sessionId));
    }
}
