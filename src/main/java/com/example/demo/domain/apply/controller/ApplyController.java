package com.example.demo.domain.apply.controller;

import com.example.demo.domain.apply.DTO.ApplyResponseDTO;
import com.example.demo.domain.apply.service.ApplyService;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

@RestController
@RequestMapping("/api/buyer/events")
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;

    @Operation(summary = "구매자 - 공연 회차 응모하기")
    @PostMapping("/{eventId}/sessions/{sessionId}/apply")
    public ApiResponse<ApplyResponseDTO> applyToSession(
            @PathVariable Long eventId,
            @PathVariable Long sessionId
    ) {
        ApplyResponseDTO responseDTO = applyService.applyToSession(eventId, sessionId);
        return ApiResponse.onSuccess(_OK, responseDTO);
    }
}
