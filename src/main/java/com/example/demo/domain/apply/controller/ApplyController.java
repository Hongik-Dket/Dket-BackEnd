package com.example.demo.domain.apply.controller;

import com.example.demo.domain.apply.dto.ApplyResponseDTO;
import com.example.demo.domain.apply.service.ApplyService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@Slf4j
@RestController
@RequestMapping("/api/buyer/concerts")
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;

    @Operation(summary = "구매자 - 공연 회차 응모하기")
    @PostMapping("/{concertId}/sessions/{sessionId}/apply")
    public ApiResponse<ApplyResponseDTO> applyToSession(
            @PathVariable Long concertId,
            @PathVariable Long sessionId
    ) {
        log.info("REQ   POST /api/buyer/concerts/{}/sessions/{}/apply", concertId, sessionId);
        ApplyResponseDTO response =  applyService.applyToSession(concertId, sessionId);
        log.info("RES   POST /api/buyer/concerts/{}/sessions/{}/apply", concertId, sessionId);
        return ApiResponse.onSuccess(_OK, response);
    }
}
