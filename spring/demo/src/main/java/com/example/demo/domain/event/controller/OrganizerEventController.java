package com.example.demo.domain.event.controller;

import com.example.demo.domain.event.dto.request.EventUploadDTO;
import com.example.demo.domain.event.dto.response.EventInfoDTO;
import com.example.demo.domain.event.dto.response.ResponseDTO;
import com.example.demo.domain.event.dto.response.SessionInfoDTO;
import com.example.demo.domain.event.service.OrganizerEventService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@Slf4j
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

    @Operation(summary = "개최자 - 공연 개최하기")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ResponseDTO> uploadEvent(@ModelAttribute EventUploadDTO request) {

        log.info("이벤트 업로드 요청 정보:");
        log.info("제목: {}", request.getTitle());
        log.info("연령 제한: {}", request.getAgeLimit());
        log.info("장소: {}", request.getLocation());
        log.info("설명: {}", request.getDescription());
        log.info("시작 날짜: {}", request.getStartDate());
        log.info("종료 날짜: {}", request.getEndDate());
        log.info("시작 시간: {}", request.getStartTime());
        log.info("종료 시간: {}", request.getEndTime());
        log.info("가격: {}", request.getPrice());
        log.info("정원: {}", request.getCapacity());
        log.info("신청 시작일: {}", request.getApplyStart());
        log.info("신청 마감일: {}", request.getApplyEnd());

        log.info("배너 파일 이름: {}", request.getBanner() != null ? request.getBanner().getOriginalFilename() : "없음");
        log.info("포스터 파일 이름: {}", request.getPoster() != null ? request.getPoster().getOriginalFilename() : "없음");

        if (request.getPhotocardList() != null) {
            for (int i = 0; i < request.getPhotocardList().size(); i++) {
                MultipartFile file = request.getPhotocardList().get(i);
                log.info("포토카드[{}] 파일 이름: {}", i + 1, file.getOriginalFilename());
            }
        } else {
            log.info("포토카드 리스트가 없습니다.");
        }

        return ApiResponse.onSuccess(_OK, organizerEventService.uploadEvent(request));
    }

}