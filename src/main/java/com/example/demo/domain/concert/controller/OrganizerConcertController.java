package com.example.demo.domain.concert.controller;

import com.example.demo.domain.concert.dto.request.ConcertUploadDTO;
import com.example.demo.domain.concert.dto.response.OrganizerConcertDetailDTO;
import com.example.demo.domain.concert.dto.response.ResponseDTO;
import com.example.demo.domain.concert.dto.response.OrganizerSessionInfoDTO;
import com.example.demo.domain.concert.service.OrganizerConcertService;
import com.example.demo.domain.concert.service.SessionService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@Slf4j
@RestController
@RequestMapping("/api/organizer/concerts")
@RequiredArgsConstructor
public class OrganizerConcertController {

    private final OrganizerConcertService organizerConcertService;
    private final SessionService sessionService;

    @Operation(summary = "개최자 - 공연 상세 조회")
    @GetMapping("/{concertId}")
    public ApiResponse<OrganizerConcertDetailDTO> getConcertDetailForOrganizer(@PathVariable Long concertId) {
        log.info("GET /api/organizer/concerts/{}/concert", concertId);
        return ApiResponse.onSuccess(_OK, organizerConcertService.getConcertDetailForOrganizer(concertId));
    }

    @Operation(summary = "개최자 - 회차 상세 조회")
    @GetMapping("/{concertId}/{sessionId}")
    public ApiResponse<OrganizerSessionInfoDTO> getSessionInfoForOrganizer(
            @PathVariable Long concertId, @PathVariable Long sessionId) {
        log.info("GET /api/organizer/concerts/{}/{}}", concertId, sessionId);
        return ApiResponse.onSuccess(_OK, organizerConcertService.getSessionInfoForOrganizer(concertId, sessionId));
    }

    @Operation(summary = "개최자 - 공연 개최하기",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "multipart/form-data")))
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ResponseDTO> uploadConcert(
            @RequestPart(name = "request") ConcertUploadDTO request,
            @RequestPart(name = "banner") MultipartFile banner,
            @RequestPart(name = "poster") MultipartFile poster,
            @RequestPart(name = "photocardList") List<MultipartFile> photoCardList
    ) {
        log.info("POST /api/organizer/concerts");
        return ApiResponse.onSuccess(_OK, organizerConcertService.uploadConcert(request, banner, poster, photoCardList));
    }

}