package com.example.demo.domain.event.controller;

import com.example.demo.domain.event.dto.request.EventUploadDTO;
import com.example.demo.domain.event.dto.response.EventInfoDTO;
import com.example.demo.domain.event.dto.response.ResponseDTO;
import com.example.demo.domain.event.dto.response.SessionInfoDTO;
import com.example.demo.domain.event.service.OrganizerEventService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @Operation(summary = "개최자 - 공연 개최하기",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "multipart/form-data")))
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ResponseDTO> uploadEvent(
            @RequestPart(name = "request") EventUploadDTO request,
            @RequestPart(name = "banner") MultipartFile banner,
            @RequestPart(name = "poster") MultipartFile poster,
            @RequestPart(name = "photocardList") List<MultipartFile> photocardList
    ) {
        return ApiResponse.onSuccess(_OK, organizerEventService.uploadEvent(request, banner, poster, photocardList));
    }

}