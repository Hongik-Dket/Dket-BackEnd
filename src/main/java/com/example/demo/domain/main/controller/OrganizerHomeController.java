package com.example.demo.domain.main.controller;

import com.example.demo.domain.main.dto.EventCardListDTO;
import com.example.demo.domain.main.dto.OrganizerHomeResponseDTO;
import com.example.demo.domain.main.service.OrganizerHomeService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/organizer/home")
@RequiredArgsConstructor
public class OrganizerHomeController {

    private final OrganizerHomeService organizerHomeService;

    @Operation(summary = "개최자 - 홈 화면 조회")
    @GetMapping("")
    public ApiResponse<OrganizerHomeResponseDTO> getOrganizerHome() {
        return ApiResponse.onSuccess(_OK, organizerHomeService.getHomeForOrganizer());
    }

    @Operation(summary = "개최자 - 오늘 공연 조회")
    @GetMapping("/today")
    public ApiResponse<EventCardListDTO> getTodayEventsOrganizer() {
        return ApiResponse.onSuccess(_OK, organizerHomeService.getTodayEventsForOrganizer());
    }

    @Operation(summary = "개최자 - 최근 응모 마감 공연 조회")
    @GetMapping("/closed")
    public ApiResponse<EventCardListDTO> getClosedEventsOrganizer() {
        return ApiResponse.onSuccess(_OK, organizerHomeService.getClosedEventsForOrganizer());
    }

    @Operation(summary = "개최자 - 개최한 전체 공연 조회")
    @GetMapping("/all")
    public ApiResponse<EventCardListDTO> getAllEventsOrganizer() {
        return ApiResponse.onSuccess(_OK, organizerHomeService.getAllEventsForOrganizer());
    }
}
